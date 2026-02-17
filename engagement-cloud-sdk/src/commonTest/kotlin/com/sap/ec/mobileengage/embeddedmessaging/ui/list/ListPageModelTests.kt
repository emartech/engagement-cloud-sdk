package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.embeddedmessaging.pagination.EmbeddedMessagingPaginationState
import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
import com.sap.ec.networking.clients.embedded.messaging.model.MessagesResponse
import com.sap.ec.networking.clients.embedded.messaging.model.Meta
import com.sap.ec.util.JsonUtil.json
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

    @BeforeTest
    fun setup() {
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
        model = ListPageModel(
            mockSdkEventDistributor,
            mockSdkLogger,
            EmbeddedMessagingPaginationState()
        )
    }

    @Test
    fun fetchMessagesWithCategories_shouldSendFetchMessagesEvent_andReturnsMessagesWithCategoriesResult_WithEndReachedTrue_onSuccess_WhenNoMorePagesCanBeFetched() =
        runTest {
            val lastPageOfMessagesResponse = createTestMessagesResponse(5, 20, 3)
            val networkResponse = Response(
                originalRequest = UrlRequest(
                    url = Url("https://example.net/embedded-messaging/api/v1/APPCODE/messages"),
                    method = HttpMethod.Get
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(),
                bodyAsText = json.encodeToString(
                    MessagesResponse.serializer(),
                    lastPageOfMessagesResponse
                )
            )
            val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "test-id",
                result = Result.success(networkResponse)
            )
            everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()) } returns mockSdkEventWaiter

            val result = model.fetchMessagesWithCategories(false, emptyList()).getOrThrow()

            result.messages shouldBe lastPageOfMessagesResponse.messages
            result.categories shouldBe lastPageOfMessagesResponse.meta.categories
            result.isEndReached shouldBe true
        }

    @Test
    fun fetchMessagesWithCategories_shouldSendFetchMessagesEvent_andReturnsMessagesWithCategoriesResult_WithEndReachedFalse_onSuccess_WhenMorePagesAvailable() =
        runTest {
            val messagesResponse = createTestMessagesResponse(10, 10, 2)
            val networkResponse = Response(
                originalRequest = UrlRequest(
                    url = Url("https://example.net/embedded-messaging/api/v1/APPCODE/messages"),
                    method = HttpMethod.Get
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(),
                bodyAsText = json.encodeToString(MessagesResponse.serializer(), messagesResponse)
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
            result.isEndReached shouldBe false
        }

    @Test
    fun fetchMessagesWithCategories_shouldSendFetchMessagesEvent_withFilterUnopenedMessagesFalse_whenFilterUnopenedOnlyIsFalse() =
        runTest {
            val testCategoryIds = listOf(1)
            val messagesResponse = createTestMessagesResponse(10, 10, 2)
            val networkResponse = Response(
                originalRequest = UrlRequest(
                    url = Url("https://example.net/embedded-messaging/api/v1/APPCODE/messages"),
                    method = HttpMethod.Get
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(),
                bodyAsText = json.encodeToString(MessagesResponse.serializer(), messagesResponse)
            )
            val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "test-id",
                result = Result.success(networkResponse)
            )
            everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()) } returns mockSdkEventWaiter

            val capturedEvent = slot<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()
            everySuspend {
                mockSdkEventDistributor.registerEvent(capture(capturedEvent))
            } returns mockSdkEventWaiter

            model.fetchMessagesWithCategories(false, testCategoryIds)

            capturedEvent.get() shouldNotBe null
            capturedEvent.get().filterUnopenedMessages shouldBe false
            capturedEvent.get().categoryIds shouldBe testCategoryIds
        }

    @Test
    fun fetchMessagesWithCategories_shouldSendFetchMessagesEvent_withFilterUnopenedMessagesTrue_whenFilterUnopenedOnlyIsTrue() =
        runTest {
            val testCategoryIds = listOf(1, 2, 3)
            val messagesResponse = createTestMessagesResponse(10, 10, 2)
            val networkResponse = Response(
                originalRequest = UrlRequest(
                    url = Url("https://example.net/embedded-messaging/api/v1/APPCODE/messages"),
                    method = HttpMethod.Get
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(),
                bodyAsText = json.encodeToString(MessagesResponse.serializer(), messagesResponse)
            )
            val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "test-id",
                result = Result.success(networkResponse)
            )
            everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()) } returns mockSdkEventWaiter

            val capturedEvent = slot<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()
            everySuspend {
                mockSdkEventDistributor.registerEvent(capture(capturedEvent))
            } returns mockSdkEventWaiter

            model.fetchMessagesWithCategories(true, testCategoryIds)

            capturedEvent.get() shouldNotBe null
            capturedEvent.get().filterUnopenedMessages shouldBe true
            capturedEvent.get().categoryIds shouldBe testCategoryIds
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
    fun fetchNextPage_shouldSendNextPageEvent_andReturnResultWithEndReachedTrue_onSuccess_whenLastPageReceived() =
        runTest {
            val lastPageOfMessagesResponse = createTestMessagesResponse(0, 10, 2)
            val networkResponse = Response(
                originalRequest = UrlRequest(
                    url = Url("https://example.net/embedded-messaging/api/v1/APPCODE/messages"),
                    method = HttpMethod.Get
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(),
                bodyAsText = json.encodeToString(
                    MessagesResponse.serializer(),
                    lastPageOfMessagesResponse
                )
            )
            val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "test-id",
                result = Result.success(networkResponse)
            )
            everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchNextPage>()) } returns mockSdkEventWaiter

            val result = model.fetchNextPage().getOrThrow()

            result.messages.size shouldBe 0
            result.messages shouldBe lastPageOfMessagesResponse.messages
            result.categories.size shouldBe 2
            result.categories shouldBe lastPageOfMessagesResponse.meta.categories
            result.isEndReached shouldBe true
        }

    @Test
    fun fetchNextPage_shouldSendNextPageEvent_andReturnResultWithEndReachedFalse_onSuccess_whenMorePagesAvailable() =
        runTest {
            val response = createTestMessagesResponse(10, 10, 2)
            val networkResponse = Response(
                originalRequest = UrlRequest(
                    url = Url("https://example.net/embedded-messaging/api/v1/APPCODE/messages"),
                    method = HttpMethod.Get
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(),
                bodyAsText = json.encodeToString(
                    MessagesResponse.serializer(),
                    response
                )
            )
            val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "test-id",
                result = Result.success(networkResponse)
            )
            everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
            everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchNextPage>()) } returns mockSdkEventWaiter

            val result = model.fetchNextPage().getOrThrow()

            result.messages shouldBe response.messages
            result.categories shouldBe response.meta.categories
            result.isEndReached shouldBe false
        }

    @Test
    fun fetchNextPage_shouldReturnFailureResult_onFailure() = runTest {
        val testException = Exception("Network error")
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response<Response>(
            originId = "test-id",
            result = Result.failure(testException)
        )
        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchNextPage>()) } returns mockSdkEventWaiter

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
    fun fetchNextPage_shouldNotSendNextPageEvent_andReturnFailureResult_whenPaginationStateEndReachedIsTrue() =
        runTest {
            model = ListPageModel(
                mockSdkEventDistributor,
                mockSdkLogger,
                EmbeddedMessagingPaginationState(isEndReached = true)
            )

            val result = model.fetchNextPage()

            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldBe "Can't fetch more pages because last page reached"
            verifySuspend { mockSdkLogger.debug("Can't fetch more messages, final page reached") }
        }

    private fun createTestMessagesResponse(
        messagesReturned: Int,
        maximumMessagesPerPageReturned: Int,
        categoriesReturned: Int
        ): MessagesResponse {
        val messages = mutableListOf<EmbeddedMessage>()
        for (num in 1..messagesReturned) {
            messages.add(
                EmbeddedMessage(
                    "$num",
                    "Title$num",
                    "testLead$num",
                    null,
                    null,
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    100000,
                    110000,
                    mapOf(),
                    "trackingInfo$num"
                )
            )
        }
        val categories = mutableListOf<MessageCategory>()
        for (num in 1..categoriesReturned) {
            categories.add(MessageCategory(num, "testCategory$num"))
        }
        val messagesResponse = MessagesResponse(
            "1.0",
            maximumMessagesPerPageReturned,
            Meta(categories),
            messages
        )
        return messagesResponse
    }
}