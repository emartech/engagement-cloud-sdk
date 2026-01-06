@file:OptIn(ExperimentalTime::class)

package com.emarsys.mobileengage.embeddedmessaging.pagination

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.embedded.messaging.model.MessagesResponse
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class EmbeddedMessagingPaginationHandlerTest {

    private lateinit var mockManager: SdkEventManagerApi
    private lateinit var flow: MutableSharedFlow<SdkEvent>
    private lateinit var mockLogger: Logger
    private lateinit var state: EmbeddedMessagingPaginationState
    private lateinit var fetchNextPageSlot: SlotCapture<SdkEvent.Internal.EmbeddedMessaging.FetchNextPage>

    private val testDispatcher = StandardTestDispatcher()
    private val json = JsonUtil.json

    @BeforeTest
    fun setUp() = runTest {
        Dispatchers.setMain(testDispatcher)
        mockManager = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)
        state = EmbeddedMessagingPaginationState()
        flow = MutableSharedFlow()
        fetchNextPageSlot = Capture.slot()
        every { mockManager.sdkEventFlow } returns flow
        everySuspend { mockLogger.debug(any<String>()) } returns Unit
        everySuspend { mockLogger.error(any<String>(), any<Throwable>()) } returns Unit
        everySuspend { mockManager.registerEvent(sdkEvent = capture(fetchNextPageSlot)) } returns mock()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        resetCalls()
        resetAnswers()
    }

    private fun createPaginationHandler(backgroundScope: CoroutineScope): EmbeddedMessagingPaginationHandler {
        return EmbeddedMessagingPaginationHandler(
            sdkEventManager = mockManager,
            applicationScope = backgroundScope,
            sdkLogger = mockLogger,
            paginationState = state
        )
    }

    @Test
    fun testConsume_FetchMessages_shouldResetAndUpdateState() = runTest {
        createPaginationHandler(backgroundScope).register()

        state.offset = 10
        state.receivedCount = 15
        state.endReached = true
        state.lastFetchMessagesId = "old-id"
        state.categoryIds = listOf(1, 2)
        state.filterUnreadMessages = false

        val fetchMessagesEvent = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
            nackCount = 0,
            offset = 0,
            categoryIds = emptyList()
        )

        flow.emit(fetchMessagesEvent)

        advanceUntilIdle()

        state.offset shouldBe 0
        state.receivedCount shouldBe 0
        state.endReached shouldBe false
        state.lastFetchMessagesId shouldBe fetchMessagesEvent.id
        state.categoryIds shouldBe emptyList()
        state.filterUnreadMessages shouldBe false
    }

    @Test
    fun testConsume_fetchMessages_shouldUpdateState_and_emit_result() = runTest {
        createPaginationHandler(backgroundScope).register()

        val fetch = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
            nackCount = 0,
            offset = 0,
            categoryIds = emptyList()
        )
        val body = buildSuccessBody(top = 3, count = 3)
        val resp = response(fetch.id, body)

        val expectedResponse: MessagesResponse = JsonUtil.json.decodeFromString(body)

        flow.emit(fetch)
        flow.emit(resp)

        advanceUntilIdle()

        state.top shouldBe 3
        state.receivedCount shouldBe 3
        state.endReached shouldBe false
        verifySuspend {
            mockManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = fetch.id,
                    result = Result.success(expectedResponse)
                )
            )
        }
    }

    @Test
    fun testConsume_fetchMessages_shouldUpdateWhenEndReached() = runTest {
        createPaginationHandler(backgroundScope).register()

        val fetch = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
            nackCount = 0,
            offset = 0,
            categoryIds = emptyList()
        )
        val body = buildSuccessBody(top = 5, count = 2)
        val resp = response(fetch.id, body)

        flow.emit(fetch)
        flow.emit(resp)

        advanceUntilIdle()

        state.receivedCount shouldBe 2
        state.top shouldBe 5
        state.endReached shouldBe true
    }

    @Test
    fun testConsume_fetchMessages_should_markEndOnEmptyPage() = runTest {
        createPaginationHandler(backgroundScope).register()

        val fetch = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
            nackCount = 0,
            offset = 0,
            categoryIds = emptyList()
        )
        val body = buildSuccessBody(top = 5, count = 0)
        val resp = response(fetch.id, body)

        flow.emit(fetch)
        flow.emit(resp)

        advanceUntilIdle()

        state.receivedCount shouldBe 0
        state.top shouldBe 5
        state.endReached shouldBe true
    }

    @Test
    fun testConsume_nextPage_shouldUpdateState() = runTest {
        createPaginationHandler(backgroundScope).register()

        val next = SdkEvent.Internal.EmbeddedMessaging.NextPage()
        state.top = 2
        state.receivedCount = 2

        flow.emit(next)

        advanceUntilIdle()

        fetchNextPageSlot.get().offset shouldBe 2
    }

    @Test
    fun testConsume_nextPage_shouldNotRegister_whenEndReached() = runTest {
        createPaginationHandler(backgroundScope).register()

        val next = SdkEvent.Internal.EmbeddedMessaging.NextPage()
        state.endReached = true

        flow.emit(next)

        advanceUntilIdle()

        (fetchNextPageSlot.value is SlotCapture.Value.Absent) shouldBe true
    }

    @Test
    fun testConsume_shouldCatchException_whenResponseParsingFails() = runTest {
        createPaginationHandler(backgroundScope).register()

        val fetch = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
            nackCount = 0,
            offset = 0,
            categoryIds = emptyList()
        )
        val response = response(fetch.id, "invalid-json")

        flow.emit(fetch)
        flow.emit(response)

        advanceUntilIdle()

        verifySuspend {
            mockLogger.error(
                "Error processing pagination event: $response",
                any<Exception>()
            )
        }
    }

    @Test
    fun testConsume_shouldIgnoreMismatchedResponse() = runTest {
        createPaginationHandler(backgroundScope).register()

        val fetch = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
            nackCount = 0,
            offset = 0,
            categoryIds = emptyList()
        )
        val resp = response("other", buildSuccessBody(4, 1))

        flow.emit(fetch); flow.emit(resp)

        advanceUntilIdle()

        state.receivedCount shouldBe 0
        state.top shouldBe 0
    }

    private fun response(originId: String, body: String) = SdkEvent.Internal.Sdk.Answer.Response(
        originId = originId,
        result = Result.success(
            Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                bodyAsText = body
            )
        )
    )

    private fun buildSuccessBody(top: Int, count: Int): String {
        val body = buildJsonObject {
            put("version", "v1")
            put("top", top)
            putJsonObject("meta") {
                put("categories", buildJsonArray { })
            }
            putJsonArray("messages") {
                repeat(count) { idx ->
                    add(
                        buildJsonObject {
                            put("id", "m$idx")
                            put("title", "t")
                            put("lead", "l")
                            put("imageUrl", "u")
                            putJsonObject("defaultAction") {
                                put("type", "OpenExternalUrl")
                                put("reporting", "{}")
                                put("url", "https://example.com")
                            }
                            putJsonArray("actions") { }
                            putJsonArray("tags") { }
                            putJsonArray("categoryIds") { }
                            put("receivedAt", 1)
                            put("expiresAt", 2)
                            putJsonObject("properties") { }
                            put("trackingInfo", "{}")
                        }
                    )
                }
            }
        }
        return json.encodeToString(body)
    }
}
