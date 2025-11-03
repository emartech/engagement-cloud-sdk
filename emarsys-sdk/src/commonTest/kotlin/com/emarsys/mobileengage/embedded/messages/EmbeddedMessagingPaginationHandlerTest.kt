package com.emarsys.mobileengage.embedded.messages

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.SdkEvent
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
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
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

@OptIn(ExperimentalCoroutinesApi::class)
class EmbeddedMessagingPaginationHandlerTest {

    private lateinit var mockManager: SdkEventManagerApi
    private lateinit var flow: MutableSharedFlow<SdkEvent>
    private lateinit var mockLogger: Logger
    private lateinit var state: EmbeddedMessagingPaginationState
    private lateinit var fetchNextPageSlot: SlotCapture<SdkEvent.Internal.EmbeddedMessaging.FetchNextPage>
    private lateinit var testScope: CoroutineScope
    private lateinit var handler: EmbeddedMessagingPaginationHandler

    private val testDispatcher = StandardTestDispatcher()
    private val json = JsonUtil.json

    @BeforeTest
    fun setUp() = runTest {
        Dispatchers.setMain(testDispatcher)
        mockManager = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)
        state = EmbeddedMessagingPaginationState()
        flow = MutableSharedFlow(replay = 0, extraBufferCapacity = Channel.UNLIMITED)
        fetchNextPageSlot = Capture.slot()
        every { mockManager.sdkEventFlow } returns flow
        everySuspend { mockLogger.debug(any<String>()) } returns Unit
        everySuspend { mockLogger.error(any<String>(), any<Throwable>()) } returns Unit
        everySuspend { mockManager.registerEvent(sdkEvent = capture(fetchNextPageSlot)) } returns mock()
        testScope = CoroutineScope(testDispatcher)

        handler = EmbeddedMessagingPaginationHandler(mockManager, testScope, mockLogger, state)
        handler.register()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        resetCalls()
        resetAnswers()
        testScope.cancel()
    }

    @Test
    fun testFetchMessagesUpdatesState() = runTest {
        val fetch = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
            nackCount = 0,
            offset = 5,
            categoryIds = listOf(7, 8)
        )

        flow.emit(fetch)

        advanceUntilIdle()

        state.offset shouldBe 5
        state.categoryIds shouldBe listOf(7, 8)
        state.lastFetchMessagesId shouldBe fetch.id
    }

    @Test
    fun testConsume_fetchMessages_shouldUpdateState() = runTest {
        val fetch = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
            nackCount = 0,
            offset = 0,
            categoryIds = emptyList()
        )
        val body = buildSuccessBody(top = 3, count = 3)
        val resp = response(fetch.id, body)

        flow.emit(fetch)
        flow.emit(resp)

        advanceUntilIdle()

        state.top shouldBe 3
        state.receivedCount shouldBe 3
        state.endReached shouldBe false
    }

    @Test
    fun testConsume_fetchMessages_shouldUpdateWhenEndReached() = runTest {
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
        val next = SdkEvent.Internal.EmbeddedMessaging.NextPage()
        state.top = 2
        state.receivedCount = 2

        flow.emit(next)

        advanceUntilIdle()

        fetchNextPageSlot.get().offset shouldBe 2
    }

    @Test
    fun testConsume_nextPage_shouldNotRegister_whenEndReached() = runTest {
        val next = SdkEvent.Internal.EmbeddedMessaging.NextPage()
        state.endReached = true

        flow.emit(next)

        advanceUntilIdle()

        (fetchNextPageSlot.value is SlotCapture.Value.Absent) shouldBe true
    }

    @Test
    fun testConsume_shouldIgnoreMismatchedResponse() = runTest {
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

    @Test
    fun testConsume_resetPaginationEvent_shouldResetsState() = runTest {
        val resetEvent = SdkEvent.Internal.EmbeddedMessaging.ResetPagination()
        val fetch = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
            nackCount = 0,
            offset = 15,
            categoryIds = listOf(3, 4, 5)
        )
        val response = response(fetch.id, buildSuccessBody(10, 2))

        flow.emit(fetch)
        flow.emit(response)

        advanceUntilIdle()

        state.offset shouldBe 15
        state.receivedCount shouldBe 2
        state.top shouldBe 10
        state.lastFetchMessagesId shouldBe fetch.id
        state.categoryIds shouldBe listOf(3, 4, 5)
        state.endReached shouldBe true


        flow.emit(resetEvent)

        advanceUntilIdle()

        state.offset shouldBe 0
        state.receivedCount shouldBe 0
        state.top shouldBe 0
        state.lastFetchMessagesId shouldBe null
        state.categoryIds shouldBe emptyList()
        state.endReached shouldBe false
        state.canFetchNextPage() shouldBe true
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
