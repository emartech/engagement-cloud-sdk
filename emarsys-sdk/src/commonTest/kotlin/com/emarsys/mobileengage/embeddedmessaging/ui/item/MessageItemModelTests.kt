package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.util.DownloaderApi
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.embeddedmessaging.models.TagOperation
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class MessageItemModelTests {
    private companion object {
        val testMessage = EmbeddedMessage(
            id = "1",
            title = "testTitle",
            lead = "testLead",
            imageUrl = "example.com",
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
    }

    private lateinit var mockDownloader: DownloaderApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi
    private lateinit var messageItemModel: MessageItemModel

    @BeforeTest
    fun setup() {
        mockDownloader = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)

        messageItemModel = createMessageItemModel()
    }

    private fun createMessageItemModel(message: EmbeddedMessage = testMessage): MessageItemModel =
        MessageItemModel(
            message = message,
            downloaderApi = mockDownloader,
            sdkEventDistributor = mockSdkEventDistributor
        )


    @Test
    fun downloadImage_shouldReturn_Null_when_ImageUrlIsMissing() = runTest {
        val result = createMessageItemModel(testMessage.copy(imageUrl = null)).downloadImage()

        result shouldBe null
        verifySuspend(VerifyMode.exactly(0)) { mockDownloader.download(any()) }
    }

    @Test
    fun downloadImage_shouldCall_downloaderApi() = runTest {
        val expectedUriString = "example.com"
        everySuspend {
            mockDownloader.download(
                expectedUriString,
                EmbeddedMessagingConstants.PLACEHOLDER_IMAGE
            )
        } returns ByteArray(0)

        messageItemModel.downloadImage()

        verifySuspend(VerifyMode.exactly(1)) {
            mockDownloader.download(
                expectedUriString,
                EmbeddedMessagingConstants.PLACEHOLDER_IMAGE
            )
        }
    }

    @Test
    fun updateTagsForMessage_shouldSendUpdateTagsForMessagesEvent_andReturnSuccess() = runTest {
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
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        val result = messageItemModel.updateTagsForMessage(
            tag = "seen",
            operation = TagOperation.Add,
            trackingInfo = "tracking-info-123"
        )

        result shouldBe true
    }

    @Test
    fun updateTagsForMessage_shouldReturnFailure_whenResponseFails() = runTest {
        val testException = Exception("Network error")
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response<Response>(
            originId = "test-id",
            result = Result.failure(testException)
        )

        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        val result = messageItemModel.updateTagsForMessage(
            tag = "seen",
            operation = TagOperation.Add,
            trackingInfo = "tracking-info-123"
        )

        result shouldBe false
    }

    @Test
    fun updateTagsForMessage_shouldCreateCorrectMessageTagUpdate() = runTest {
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
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        messageItemModel.updateTagsForMessage(
            tag = "seen",
            operation = TagOperation.Add,
            trackingInfo = "tracking-info-123"
        )

        verifySuspend { mockSdkEventDistributor.registerEvent(any()) }
    }
}