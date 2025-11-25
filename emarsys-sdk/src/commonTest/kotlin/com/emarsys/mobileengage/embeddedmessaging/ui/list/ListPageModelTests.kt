package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import com.emarsys.networking.clients.embedded.messaging.model.MessagesResponse
import com.emarsys.networking.clients.embedded.messaging.model.Meta
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ListPageModelTests {
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var model: ListPageModel
    private val json = JsonUtil.json

    @BeforeTest
    fun setup() {
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
        model = ListPageModel(mockSdkEventDistributor, mockSdkLogger)
    }

    @Test
    fun fetchMessagesWithCategories_shouldSendFetchMessagesEvent_andReturnsMessagesWithCategoriesResult_onSuccess() =
        runTest {
            val messagesResponse = createTestMessageResponse()

            val networkResponse = Response(
                originalRequest = UrlRequest(Url("https://example.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = headersOf(),
                bodyAsText = json.encodeToString(messagesResponse)
            )
            val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "test-id",
                result = Result.success(networkResponse)
            )
            everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()) } returns mockSdkEventWaiter

            val result = model.fetchMessagesWithCategories(false, emptyList()).getOrThrow()

            result.messages shouldBe messagesResponse.messages
            result.categories shouldBe messagesResponse.meta.categories
        }

    @Test
    fun fetchMessagesWithCategories_shouldReturnFailureResult_whenResponseFails() = runTest {
        val testException = Exception("Network error")
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response<Response>(
            originId = "test-id",
            result = Result.failure(testException)
        )

        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()) } returns mockSdkEventWaiter

        val result = model.fetchMessagesWithCategories(false, emptyList())

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe testException
        verifySuspend { mockSdkLogger.error("FetchMessagesWithCategories failure.", testException) }
    }

    @Test
    fun fetchMessagesWithCategories_shouldLogAndReturnFailureResult_onException() = runTest {
        val testException = Exception("Test exception")

        everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()) } throws testException

        val result = model.fetchMessagesWithCategories(false, emptyList())

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe testException
        verifySuspend {
            mockSdkLogger.error(
                "FetchMessagesWithCategories exception.",
                testException
            )
        }
    }

    @Test
    fun fetchBadgeCount_shouldSendFetchBadgeCountEvent_andReturnBadgeCountFromResponse() = runTest {
        val networkResponse = Response(
            originalRequest = UrlRequest(Url("https://example.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = headersOf(),
            bodyAsText = """{"version":"1.0","count":5}"""
        )
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
            originId = "test-id",
            result = Result.success(networkResponse)
        )

        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        val result = model.fetchBadgeCount()

        result shouldBe 5
    }

    @Test
    fun fetchBadgeCount_shouldReturnZero_whenResponseFails() = runTest {
        val testException = Exception("Network error")
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response<Response>(
            originId = "test-id",
            result = Result.failure(testException)
        )

        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        val result = model.fetchBadgeCount()

        result shouldBe 0
        verifySuspend { mockSdkLogger.error("FetchBadgeCount failure.", testException) }
    }

    @Test
    fun fetchBadgeCount_shouldLogAndReturnZero_onException() = runTest {
        val testException = Exception("Test exception")

        everySuspend { mockSdkEventDistributor.registerEvent(any()) } throws testException

        val result = model.fetchBadgeCount()

        result shouldBe 0
        verifySuspend { mockSdkLogger.error("FetchBadgeCount exception.", testException) }
    }

    @Test
    fun fetchNextPage_shouldSendNextPageEvent_andReturnResult_onSuccess() = runTest {
        val response = createTestMessageResponse()
        val networkResponse = Response(
            originalRequest = UrlRequest(Url("https://example.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = headersOf(),
            bodyAsText = json.encodeToString(response)
        )
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
            originId = "test-id",
            result = Result.success(networkResponse)
        )

        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.NextPage>()) } returns mockSdkEventWaiter

        val result = model.fetchNextPage().getOrThrow()

        result.messages.size shouldBe 2
        result.messages shouldBe response.messages

        result.categories.size shouldBe 2
        result.categories shouldBe response.meta.categories
    }

    @Test
    fun fetchNextPage_shouldReturnFailureResult_onFailure() = runTest {
        val testException = Exception("Network error")
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response<Response>(
            originId = "test-id",
            result = Result.failure(testException)
        )

        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        val result = model.fetchNextPage()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe testException
        verifySuspend { mockSdkLogger.error("FetchNextPage failure.", testException) }
    }

    @Test
    fun fetchNextPage_shouldReturnFailureResult_onException() = runTest {
        val testException = Exception("Test exception")

        everySuspend { mockSdkEventDistributor.registerEvent(any()) } throws testException

        val result = model.fetchNextPage()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe testException
        verifySuspend { mockSdkLogger.error("FetchNextPage exception.", testException) }
    }

    @Test
    fun fetchMessagesWithCategories_shouldSendFetchMessagesEvent_withFilterUnreadMessagesFalse_whenFilterUnreadOnlyIsFalse() =
        runTest {
            val networkResponse = Response(
                originalRequest = UrlRequest(Url("https://example.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = headersOf(),
                bodyAsText = """{"version":"1.0","top":10,"meta":{"categories":[]},"messages":[]}"""
            )
            val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "test-id",
                result = Result.success(networkResponse)
            )

            everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse

            val capturedEvent = slot<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()
            everySuspend {
                mockSdkEventDistributor.registerEvent(capture(capturedEvent))
            } returns mockSdkEventWaiter

            model.fetchMessagesWithCategories(false, emptyList())

            capturedEvent.get() shouldNotBe null
            capturedEvent.get().filterUnreadMessages shouldBe false
        }

    @Test
    fun fetchMessagesWithCategories_shouldSendFetchMessagesEvent_withFilterUnreadMessagesTrue_whenFilterUnreadOnlyIsTrue() =
        runTest {
            val expectedCategoryIds = listOf(1, 2, 3)
            val networkResponse = Response(
                originalRequest = UrlRequest(Url("https://example.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = headersOf(),
                bodyAsText = """{"version":"1.0","top":10,"meta":{"categories":[]},"messages":[]}"""
            )
            val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "test-id",
                result = Result.success(networkResponse)
            )

            everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse

            val capturedEvent = slot<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()
            everySuspend {
                mockSdkEventDistributor.registerEvent(capture(capturedEvent))
            } returns mockSdkEventWaiter

            model.fetchMessagesWithCategories(filterUnreadOnly = true, expectedCategoryIds)

            with(capturedEvent.get()) {
                this shouldNotBe null
                filterUnreadMessages shouldBe true
                categoryIds shouldBe expectedCategoryIds
            }
        }

    private fun createTestMessageResponse(): MessagesResponse {
        val categories = listOf(
            MessageCategory(1, "testCategory1"),
            MessageCategory(2, "testCategory2")
        )
        val messagesResponse = MessagesResponse(
            "1.0",
            10,
            Meta(categories),
            listOf(
                EmbeddedMessage(
                    "1",
                    "Title1",
                    "testLead",
                    null,
                    null,
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    100000,
                    110000,
                    mapOf(),
                    "trackingInfo"
                ),
                EmbeddedMessage(
                    "2",
                    "Title2",
                    "testLead",
                    null,
                    null,
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    100000,
                    110000,
                    mapOf(),
                    "trackingInfo"
                ),
            ),
        )
        return messagesResponse
    }
}