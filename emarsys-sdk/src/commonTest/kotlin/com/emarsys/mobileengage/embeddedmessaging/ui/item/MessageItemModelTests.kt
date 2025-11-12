package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.util.DownloaderApi
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.embeddedmessaging.models.MessageTagUpdate
import com.emarsys.mobileengage.embeddedmessaging.models.TagOperation
import com.emarsys.mobileengage.embeddedmessaging.provider.FallbackImageProviderApi
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class MessageItemModelTests {

    private lateinit var mockDownloader: DownloaderApi
    private lateinit var mockFallbackImageProvider: FallbackImageProviderApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi

    @BeforeTest
    fun setup() {
        mockDownloader = mock()
        mockFallbackImageProvider = mock()
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
    }

    @Test
    fun downloadImage_shouldReturn_Null_when_ImageUrlIsMissing() = runTest {
        val testMessage = EmbeddedMessage(
            id = "1",
            title = "testTitle",
            lead = "testLead",
            imageUrl = null,
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
        val model = MessageItemModel(
            message = testMessage,
            downloaderApi = mockDownloader,
            fallbackImageProvider = mockFallbackImageProvider,
            sdkEventDistributor = mockSdkEventDistributor
        )

        val result = model.downloadImage()

        result shouldBe null
        verifySuspend(VerifyMode.exactly(0)) { mockDownloader }
    }

    @Test
    fun downloadImage_shouldCall_downloaderApi() = runTest {
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
        val model = MessageItemModel(
            message = testMessage,
            downloaderApi = mockDownloader,
            fallbackImageProvider = mockFallbackImageProvider,
            sdkEventDistributor = mockSdkEventDistributor
        )
        everySuspend { mockDownloader.download("example.com") }.returns(ByteArray(0))

        model.downloadImage()

        verifySuspend(VerifyMode.exactly(1)) { mockDownloader }
    }

    @Test
    fun updateTagsForMessage_shouldSendUpdateTagsForMessagesEvent_andReturnSuccess() = runTest {
        val testMessage = EmbeddedMessage(
            id = "message-1",
            title = "testTitle",
            lead = "testLead",
            imageUrl = null,
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "tracking-info-123"
        )
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

        val model = MessageItemModel(
            message = testMessage,
            downloaderApi = mockDownloader,
            fallbackImageProvider = mockFallbackImageProvider,
            sdkEventDistributor = mockSdkEventDistributor
        )

        val result = model.updateTagsForMessage(tag = "seen", operation = TagOperation.Add, trackingInfo = "tracking-info-123")

        result shouldBe true
    }

    @Test
    fun updateTagsForMessage_shouldReturnFailure_whenResponseFails() = runTest {
        val testMessage = EmbeddedMessage(
            id = "message-1",
            title = "testTitle",
            lead = "testLead",
            imageUrl = null,
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "tracking-info-123"
        )
        val testException = Exception("Network error")
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response<Response>(
            originId = "test-id",
            result = Result.failure(testException)
        )

        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        val model = MessageItemModel(
            message = testMessage,
            downloaderApi = mockDownloader,
            fallbackImageProvider = mockFallbackImageProvider,
            sdkEventDistributor = mockSdkEventDistributor
        )

        val result = model.updateTagsForMessage(tag = "seen", operation = TagOperation.Add, trackingInfo = "tracking-info-123")

        result shouldBe false
    }

    @Test
    fun updateTagsForMessage_shouldCreateCorrectMessageTagUpdate() = runTest {
        val testMessage = EmbeddedMessage(
            id = "message-1",
            title = "testTitle",
            lead = "testLead",
            imageUrl = null,
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "tracking-info-123"
        )
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

        val model = MessageItemModel(
            message = testMessage,
            downloaderApi = mockDownloader,
            fallbackImageProvider = mockFallbackImageProvider,
            sdkEventDistributor = mockSdkEventDistributor
        )

        model.updateTagsForMessage(tag = "seen", operation = TagOperation.Add, trackingInfo = "tracking-info-123")

        verifySuspend {
            mockSdkEventDistributor.registerEvent(any())
        }
    }
}