package com.sap.ec.mobileengage.embeddedmessaging.ui.item

import com.sap.ec.context.DefaultUrlsApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.util.DownloaderApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.ActionFactoryApi
import com.sap.ec.mobileengage.action.actions.Action
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.sap.ec.mobileengage.action.models.BasicRichContentDisplayActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.embeddedmessaging.models.MessageTagUpdate
import com.sap.ec.mobileengage.embeddedmessaging.models.TagOperation
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants
import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessageAnimation
import com.sap.ec.networking.clients.embedded.messaging.model.ListThumbnailImage
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class MessageItemModelTests {
    private companion object {
        const val EMBEDDED_MESSAGING_BASE_URL = "https://example.com/url"
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
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var mockLogger: Logger
    private lateinit var messageItemModel: MessageItemModel

    private val eventSlot = slot<SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages>()

    @BeforeTest
    fun setup() {
        mockDownloader = mock(MockMode.autofill)
        mockDefaultUrls = mock(MockMode.autofill)
        every { mockDefaultUrls.embeddedMessagingBaseUrl } returns EMBEDDED_MESSAGING_BASE_URL
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        mockActionFactory = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)

        messageItemModel = createMessageItemModel()
    }

    private fun createMessageItemModel(message: EmbeddedMessage = TEST_MESSAGE): MessageItemModel =
        MessageItemModel(
            message = message,
            sdkContext = mockSdkContext,
            downloader = mockDownloader,
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
                    row("opened", messageItemModel::tagMessageOpened),
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
                updateData shouldBe MessageTagUpdate(
                    messageId = messageItemModel.message.id,
                    operation = TagOperation.Add,
                    tag = expectedTag,
                    trackingInfo = messageItemModel.message.trackingInfo
                )

            }
        }
    }

    @Test
    fun tagMessageOpened_shouldPropagateCancellationException() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } throws CancellationException("coroutine cancelled")

        shouldThrow<CancellationException> {
            messageItemModel.tagMessageOpened()
        }
    }

    @Test
    fun deleteMessage_shouldPropagateCancellationException() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } throws CancellationException("coroutine cancelled")

        shouldThrow<CancellationException> {
            messageItemModel.deleteMessage()
        }
    }

    @Test
    fun tagMessageRead_shouldPropagateCancellationException() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } throws CancellationException("coroutine cancelled")

        shouldThrow<CancellationException> {
            messageItemModel.tagMessageRead()
        }
    }

    @Test
    fun isNotOpened_shouldReturnFalse_whenTagsContainingOpened() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("opened")))

        model.isNotOpened() shouldBe false
    }

    @Test
    fun isNotOpened_shouldReturnTrue_whenTagsDontContainOpened() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("custom")))

        model.isNotOpened() shouldBe true
    }

    @Test
    fun isNotOpened_shouldReturnFalse_whenTagsContainsOpened_caseInsensitively() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("Opened")))

        model.isNotOpened() shouldBe false
    }

    @Test
    fun isRead_shouldReturnTrue_whenTagsContainingRead() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("read")))

        model.isRead() shouldBe true
    }

    @Test
    fun isRead_shouldReturnFalse_whenTagsDontContainRead() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("custom")))

        model.isRead() shouldBe false
    }

    @Test
    fun isRead_shouldReturnTrue_whenTagsContainRead_caseInsensitively() = runTest {
        val model = createMessageItemModel(TEST_MESSAGE.copy(tags = listOf("Read")))

        model.isRead() shouldBe true
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

            model.hasRichContent() shouldBe true
        }

    @Test
    fun shouldNavigate_shouldReturnFalse_whenDefaultActionIsNotRichContentDisplayAction() =
        runTest {
            val model = createMessageItemModel(
                TEST_MESSAGE.copy(
                    defaultAction = BasicOpenExternalUrlActionModel(url = "https://example.com")
                )
            )

            model.hasRichContent() shouldBe false
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

    @Test
    fun updateTagsForMessage_shouldPropagateCancellationException_whenCoroutineIsCancelled() =
        runTest {
            val testJob = Job()
            val testScope = CoroutineScope(StandardTestDispatcher(testScheduler) + testJob)

            everySuspend { mockSdkEventDistributor.registerEvent(any()) } calls {
                testJob.cancel()
                throw CancellationException("Job was cancelled")
            }

            var result: Result<Unit>? = null
            testScope.launch {
                result = messageItemModel.tagMessageOpened()
            }
            advanceUntilIdle()

            result shouldBe null
        }
}