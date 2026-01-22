package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.util.DownloaderApi
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.BasicRichContentDisplayActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.embeddedmessaging.models.MessageTagUpdate
import com.emarsys.mobileengage.embeddedmessaging.models.TagOperation
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants
import com.emarsys.networking.clients.embedded.messaging.model.Category
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessageAnimation
import com.emarsys.networking.clients.embedded.messaging.model.ListThumbnailImage
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.ExperimentalTime

class MessageItemModelTests {
    private companion object {
        val TEST_MESSAGE = EmbeddedMessage(
            id = "1",
            title = "testTitle",
            lead = "testLead",
            listThumbnailImage = ListThumbnailImage("example.com", null),
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categories = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
        val FALLBACK_IMAGE_BYTEARRAY = Base64.decode(
            EmbeddedMessagingConstants.Image.BASE64_PLACEHOLDER_IMAGE
                .encodeToByteArray()
        )
        val TEST_ACTION = BasicRichContentDisplayActionModel(
            url = "https://example.com",
            animation = EmbeddedMessageAnimation.FADING_FROM_BOTTOM
        )
    }

    private lateinit var mockDownloader: DownloaderApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi
    private lateinit var mockActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var mockLogger: Logger
    private lateinit var messageItemModel: MessageItemModel

    private val eventSlot = slot<SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages>()

    @BeforeTest
    fun setup() {
        mockDownloader = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        mockActionFactory = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)

        messageItemModel = createMessageItemModel()
    }

    private fun createMessageItemModel(message: EmbeddedMessage = TEST_MESSAGE): MessageItemModel =
        MessageItemModel(
            message = message,
            downloaderApi = mockDownloader,
            sdkEventDistributor = mockSdkEventDistributor,
            actionFactory = mockActionFactory,
            logger = mockLogger
        )


    @Ignore
    @Test
    fun downloadImage_shouldReturn_Null_when_ImageUrlIsMissing() = runTest {
        val result =
            createMessageItemModel(TEST_MESSAGE.copy(listThumbnailImage = null)).downloadImage()

        result shouldBe null
        verifySuspend(VerifyMode.exactly(0)) { mockDownloader.download(any()) }
    }

    @Test
    fun downloadImage_shouldCall_downloaderApi() = runTest {
        val fallbackSlot: SlotCapture<ByteArray> = slot()
        val expectedUriString = "example.com"
        everySuspend {
            mockDownloader.download(
                expectedUriString,
                capture(fallbackSlot)
            )
        } returns ByteArray(0)

        messageItemModel.downloadImage()

        fallbackSlot.get() shouldBe FALLBACK_IMAGE_BYTEARRAY
        verifySuspend(VerifyMode.exactly(1)) {
            mockDownloader.download(
                expectedUriString,
                any()
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun messageTagging_shouldSendUpdateTagsForMessagesEvent_andReturnSuccess() = runTest {
        forAll(
            table(
                headers("expectedTag", "testMethod"),
                listOf(
                    row("deleted", messageItemModel::deleteMessage),
                    row("read", messageItemModel::tagMessageRead),
                )
            )
        ) { expectedTag, testMethod ->
            val networkResponse = Response(
                originalRequest = UrlRequest(Url("https://example.com"), HttpMethod.Patch),
                status = HttpStatusCode.OK,
                headers = headersOf(),
                bodyAsText = ""
            )
            val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "test-id",
                result = Result.success(networkResponse)
            )

            everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
            everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockSdkEventWaiter

            val result = testMethod()

            result.isSuccess shouldBe true
            eventSlot.get().run {
                nackCount shouldBe 0
                updateData shouldBe listOf(
                    MessageTagUpdate(
                        messageId = messageItemModel.message.id,
                        operation = TagOperation.Add,
                        tag = expectedTag,
                        trackingInfo = messageItemModel.message.trackingInfo
                    )
                )
            }
        }
    }

    @Test
    fun messageTagging_shouldReturnFailure_whenResponseFails() = runTest {
        forAll(
            table(
                headers("testMethod"),
                listOf(
                    row(messageItemModel::deleteMessage),
                    row(messageItemModel::tagMessageRead),
                )
            )
        ) { testMethod ->
            val testException = Exception("Network error")
            val answerResponse = SdkEvent.Internal.Sdk.Answer.Response<Response>(
                originId = "test-id",
                result = Result.failure(testException)
            )

            everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

            val result = testMethod()

            result.isSuccess shouldBe false
        }
    }

    @Test
    fun isUnread_shouldReturnFalse_whenTagsContainingRead() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("read")))

        model.isUnread() shouldBe false
    }

    @Test
    fun isUnread_shouldReturnTrue_whenTagsDontContainRead() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("unread")))

        model.isUnread() shouldBe true
    }

    @Test
    fun isUnread_shouldReturnFalse_whenTagsContainsRead_caseInsensitively() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("Read")))

        model.isUnread() shouldBe false
    }

    @Test
    fun isPinned_shouldReturnFalse_whenTagsNotContainingPinned() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("notPinned")))

        model.isPinned() shouldBe false
    }

    @Test
    fun isPinned_shouldReturnTrue_whenTagsContainsPinned() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("pinned")))

        model.isPinned() shouldBe true
    }

    @Test
    fun isPinned_shouldReturnTrue_whenTagsContainsPinned_caseInsensitively() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("PinNed")))

        model.isPinned() shouldBe true
    }

    @Test
    fun shouldNavigate_shouldReturnTrue_whenMessageContainsRichContentDisplayActionAsDefaultAction() =
        runTest {
            val model = createMessageItemModel(TEST_MESSAGE.copy(defaultAction = TEST_ACTION))

            model.shouldNavigate() shouldBe true
        }

    @Test
    fun shouldNavigate_shouldReturnFalse_whenDefaultActionIsNotRichContentDisplayAction() =
        runTest {
            val model = createMessageItemModel(
                TEST_MESSAGE.copy(
                    defaultAction = BasicOpenExternalUrlActionModel(url = "https://example.com")
                )
            )

            model.shouldNavigate() shouldBe false
        }

    @Test
    fun handleDefaultAction_shouldCreateActionWithFactory_andInvoke() = runTest {
        val mockAction = mock<Action<Unit>>(MockMode.autofill)
        everySuspend { mockActionFactory.create(TEST_ACTION) } returns mockAction
        val model = createMessageItemModel(TEST_MESSAGE.copy(defaultAction = TEST_ACTION))

        model.handleDefaultAction()

        verifySuspend { mockActionFactory.create(TEST_ACTION) }
        verifySuspend { mockAction() }
    }
}