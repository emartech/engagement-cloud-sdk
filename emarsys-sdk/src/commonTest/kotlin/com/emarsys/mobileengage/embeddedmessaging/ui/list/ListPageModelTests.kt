package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.networking.clients.embedded.messaging.model.BadgeCountResponse
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.networking.clients.embedded.messaging.model.MessagesResponse
import com.emarsys.networking.clients.embedded.messaging.model.Meta
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.matcher.any
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ListPageModelTests {

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi

    @Test
    fun fetchMessages_shouldSendFetchMessagesEvent_andReturnMessagesFromResponse() = runTest {
        val networkResponse = Response(
            originalRequest = UrlRequest(Url("https://example.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = headersOf(),
            bodyAsText = """{"version":"1.0","top":10,"meta":{"categories":[]},"messages":[{"id":"1","title":"Title1","lead":"testLead","imageUrl":null,"defaultAction":null,"actions":[],"tags":[],"categoryIds":[],"receivedAt":100000,"expiresAt":110000,"properties":{},"trackingInfo":"anything"},{"id":"2","title":"Title2","lead":"testLead","imageUrl":null,"defaultAction":null,"actions":[],"tags":[],"categoryIds":[],"receivedAt":100000,"expiresAt":110000,"properties":{},"trackingInfo":"anything"}]}"""
        )
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
            originId = "test-id",
            result = Result.success(networkResponse)
        )

        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()) } returns mockSdkEventWaiter

        val model = ListPageModel(mockSdkEventDistributor)

        val result = model.fetchMessages()

        result.size shouldBe 2
        result[0].id shouldBe "1"
        result[0].title shouldBe "Title1"
        result[1].id shouldBe "2"
        result[1].title shouldBe "Title2"
    }

    @Test
    fun fetchMessages_shouldReturnEmptyList_whenResponseFails() = runTest {
        val testException = Exception("Network error")
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response<Response>(
            originId = "test-id",
            result = Result.failure(testException)
        )

        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMessages>()) } returns mockSdkEventWaiter

        val model = ListPageModel(mockSdkEventDistributor)

        val result = model.fetchMessages()

        result shouldBe emptyList()
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

        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        val model = ListPageModel(mockSdkEventDistributor)

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

        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        val model = ListPageModel(mockSdkEventDistributor)

        val result = model.fetchBadgeCount()

        result shouldBe 0
    }

    @Test
    fun fetchNextPage_shouldSendFetchNextPageEvent_andReturnMessagesFromResponse() = runTest {
        val networkResponse = Response(
            originalRequest = UrlRequest(Url("https://example.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = headersOf(),
            bodyAsText = """{"version":"1.0","top":10,"meta":{"categories":[]},"messages":[{"id":"3","title":"Title3","lead":"testLead","imageUrl":null,"defaultAction":null,"actions":[],"tags":[],"categoryIds":[],"receivedAt":100000,"expiresAt":110000,"properties":{},"trackingInfo":"anything"},{"id":"4","title":"Title4","lead":"testLead","imageUrl":null,"defaultAction":null,"actions":[],"tags":[],"categoryIds":[],"receivedAt":100000,"expiresAt":110000,"properties":{},"trackingInfo":"anything"}]}"""
        )
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response(
            originId = "test-id",
            result = Result.success(networkResponse)
        )

        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        val model = ListPageModel(mockSdkEventDistributor)

        val result = model.fetchNextPage(offset = 2, categoryIds = emptyList())

        result.size shouldBe 2
        result[0].id shouldBe "3"
        result[0].title shouldBe "Title3"
        result[1].id shouldBe "4"
        result[1].title shouldBe "Title4"
    }

    @Test
    fun fetchNextPage_shouldReturnEmptyList_whenResponseFails() = runTest {
        val testException = Exception("Network error")
        val answerResponse = SdkEvent.Internal.Sdk.Answer.Response<Response>(
            originId = "test-id",
            result = Result.failure(testException)
        )

        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        everySuspend { mockSdkEventWaiter.await<Response>() } returns answerResponse
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mockSdkEventWaiter

        val model = ListPageModel(mockSdkEventDistributor)

        val result = model.fetchNextPage(offset = 2, categoryIds = emptyList())

        result shouldBe emptyList()
    }
}