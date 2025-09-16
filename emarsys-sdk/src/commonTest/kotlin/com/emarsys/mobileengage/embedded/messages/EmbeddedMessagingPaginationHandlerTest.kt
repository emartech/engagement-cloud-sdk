package com.emarsys.mobileengage.embedded.messages

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.SdkEvent
import dev.mokkery.*
import dev.mokkery.answering.returns
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmbeddedMessagingPaginationHandlerTest {

    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var mockSdkLogger: Logger
    private lateinit var paginationState: EmbeddedMessagingPaginationState
    private lateinit var fetchNextPageCaptor: SlotCapture<SdkEvent.Internal.EmbeddedMessaging.FetchNextPage>


    @BeforeTest
    fun setup() = runTest {
        mockSdkEventManager = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
        paginationState = spy(EmbeddedMessagingPaginationState())
        fetchNextPageCaptor = Capture.slot<SdkEvent.Internal.EmbeddedMessaging.FetchNextPage>()
        everySuspend { mockSdkEventManager.registerEvent(sdkEvent = capture(fetchNextPageCaptor)) } returns mock(MockMode.autofill)
        sdkEventFlow = MutableSharedFlow<SdkEvent>(
            replay = 100,
            extraBufferCapacity = Channel.UNLIMITED
        )
        everySuspend { mockSdkEventManager.sdkEventFlow } returns sdkEventFlow
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    private fun createEmbeddedMessagingPaginationHandler(applicationScope: CoroutineScope) =
        EmbeddedMessagingPaginationHandler(
            sdkEventManager = mockSdkEventManager,
            applicationScope = applicationScope,
            sdkLogger = mockSdkLogger,
            paginationState = paginationState
        )

    @Test
    fun testRegister_should_log_event() = runTest {
        createEmbeddedMessagingPaginationHandler(backgroundScope).register()
        everySuspend {
            mockSdkLogger.debug(any<String>())
        }
        verifySuspend(VerifyMode.exactly(1)) {
            mockSdkLogger.debug(any<String>())
        }
    }

    @Test
    fun testConsume_should_consume_fetchMessages_events_and_save_offset_and_categoryIds_to_PaginationState() =
        runTest {
            createEmbeddedMessagingPaginationHandler(backgroundScope).register()
            paginationState.offset shouldBe 0
            paginationState.categoryIds shouldBe emptyList()

            val event =
                SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                    nackCount = 0,
                    offset = 10,
                    categoryIds = listOf(1, 2, 3)
                )

            everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit

            val sdkEvents = backgroundScope.async {
                sdkEventFlow.take(1).toList()
            }

            sdkEventFlow.emit(event)

            advanceUntilIdle()

            sdkEvents.await() shouldBe listOf(event)

            paginationState.offset shouldBe event.offset
            paginationState.categoryIds shouldBe event.categoryIds
        }

    @Test
    fun testConsume_should_save_Answer_to_PaginationState_only_when_originId_equals_lastFetchMessageId_and_Result_Response_isSuccess() =
        runTest {
            createEmbeddedMessagingPaginationHandler(backgroundScope).register()

            val expectedTop = 30

            val fetchMessagesEvent =
                SdkEvent.Internal.EmbeddedMessaging.FetchMessages(nackCount = 0, offset = 0, categoryIds = emptyList())

            val expectedEventResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = fetchMessagesEvent.id,
                Result.success(
                    Response(
                        originalRequest = UrlRequest(
                            url = Url("test.com"),
                            method = HttpMethod.Get
                        ),
                        status = HttpStatusCode.OK,
                        headers = Headers.Empty,
                        bodyAsText = createSuccessResponse(expectedTop)
                    )
                )
            )
            val wrongEventResponse = SdkEvent.Internal.Sdk.Answer.Response(
                originId = "DEFINITELY_NOT_EVENT1's_ID",
                Result.success(
                    Response(
                        originalRequest = UrlRequest(
                            url = Url("test.com"),
                            method = HttpMethod.Get
                        ),
                        status = HttpStatusCode.OK,
                        headers = Headers.Empty,
                        bodyAsText = createSuccessResponse(expectedTop)
                    )
                )
            )
            everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit

            val sdkEvents = backgroundScope.async {
                sdkEventFlow.take(3).toList()
            }

            sdkEventFlow.emit(fetchMessagesEvent)
            sdkEventFlow.emit(expectedEventResponse)
            sdkEventFlow.emit(wrongEventResponse)

            advanceUntilIdle()

            sdkEvents.await() shouldBe listOf(fetchMessagesEvent, expectedEventResponse, wrongEventResponse)

            paginationState.lastFetchMessagesId shouldBe fetchMessagesEvent.id
            paginationState.top shouldBe expectedTop
        }

    @Test
    fun testConsume_should_not_consume_Answer_if_Answer_isFailure() = runTest {
        createEmbeddedMessagingPaginationHandler(backgroundScope).register()
        paginationState.offset shouldBe 0
        paginationState.categoryIds shouldBe emptyList()
        paginationState.top shouldBe 0
        paginationState.lastFetchMessagesId shouldBe null
        val fetchMessagesEvent =
            SdkEvent.Internal.EmbeddedMessaging.FetchMessages(nackCount = 0, offset = 10, categoryIds = listOf(1))
        val failureEventResponse = SdkEvent.Internal.Sdk.Answer.Response(
            originId = fetchMessagesEvent.id,
            Result.failure<Exception>(
                Exception("Something went wrong")
            )
        )
        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit

        val sdkEvents = backgroundScope.async {
            sdkEventFlow.take(2).toList()
        }

        sdkEventFlow.emit(fetchMessagesEvent)
        sdkEventFlow.emit(failureEventResponse)

        advanceUntilIdle()

        sdkEvents.await() shouldBe listOf(fetchMessagesEvent, failureEventResponse)
        paginationState.lastFetchMessagesId shouldBe fetchMessagesEvent.id
        paginationState.offset shouldBe 10
        paginationState.top shouldBe 0
    }

    @Test
    fun testConsume_should_consume_NextPage_Response_and_FetchMessages_events() =
        runTest {
            createEmbeddedMessagingPaginationHandler(backgroundScope).register()
            val event1 =
                SdkEvent.Internal.EmbeddedMessaging.FetchMessages(nackCount = 0, offset = 0, categoryIds = emptyList())


            val event2 = SdkEvent.Internal.Sdk.Answer.Response(
                originId = event1.id,
                Result.success(
                    Response(
                        originalRequest = UrlRequest(
                            url = Url("test.com"),
                            method = HttpMethod.Companion.Get
                        ),
                        status = HttpStatusCode.Companion.OK,
                        headers = Headers.Companion.Empty,
                        bodyAsText = createSuccessResponse()
                    )
                )
            )
            val event3 = SdkEvent.Internal.EmbeddedMessaging.NextPage()
            val event4 = SdkEvent.Internal.EmbeddedMessaging.FetchMeta(nackCount = 0)
            everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit

            val sdkEvents = backgroundScope.async {
                sdkEventFlow.take(4).toList()
            }

            sdkEventFlow.emit(event1)
            sdkEventFlow.emit(event2)
            sdkEventFlow.emit(event3)
            sdkEventFlow.emit(event4)

            advanceUntilIdle()

            sdkEvents.await() shouldBe listOf(event1, event2, event3, event4)
            verifySuspend(VerifyMode.exactly(1)) { paginationState.offset = any() }
            verifySuspend(VerifyMode.exactly(1)) { paginationState.top = any() }
            verifySuspend(VerifyMode.exactly(1)) { paginationState.canFetchNextPage() }
        }

    @Test
    fun testConsume_should_register_FetchNextPageEvent_with_propper_data_mapped_from_NextPage_event_when_consume_happens_notOnLastPage() = runTest {
            createEmbeddedMessagingPaginationHandler(backgroundScope).register()
            paginationState.lastFetchMessagesId = "any"
            paginationState.top = 30
            paginationState.offset = 0
            paginationState.count = 100
            paginationState.categoryIds = listOf(1)

            val nextPageEvent = SdkEvent.Internal.EmbeddedMessaging.NextPage()
            everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit
            val sdkEvents = backgroundScope.async {
                sdkEventFlow.take(1).toList()
            }

            sdkEventFlow.emit(nextPageEvent)
            advanceUntilIdle()

            sdkEvents.await() shouldBe listOf(nextPageEvent)
            val captured = fetchNextPageCaptor.get()
            captured.id shouldBe nextPageEvent.id
            captured.type shouldBe "fetchNextPage"
            verifySuspend { mockSdkEventManager.registerEvent(captured) }
        }

    @Test
    fun testConsume_shouldNotTriggerFetchNextPage_when_NextPage_consume_happens_OnLastPage() = runTest {
        createEmbeddedMessagingPaginationHandler(backgroundScope).register()
        paginationState.lastFetchMessagesId = "any"
        paginationState.top = 30
        paginationState.offset = 0
        paginationState.count = 10
        paginationState.categoryIds = listOf(1)

        val nextPageEvent = SdkEvent.Internal.EmbeddedMessaging.NextPage()
        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit
        val sdkEvents = backgroundScope.async {
            sdkEventFlow.take(1).toList()
        }

        sdkEventFlow.emit(nextPageEvent)
        advanceUntilIdle()

        sdkEvents.await() shouldBe listOf(nextPageEvent)
        val captured = fetchNextPageCaptor.value
        println(captured)
        captured shouldBe SlotCapture.Value.Absent
    }

    private fun createSuccessResponse(expectedTop: Int = 0, expectedCount: Int = 100): String = """{
                              "version": "v1",
                              "count": $expectedCount,
                              "top": $expectedTop,
                              "meta": {
                                "categories": [
                                  {
                                    "id": 123,
                                    "value": "System"
                                  }
                                ]
                              },
                              "messages": [
                                {
                                  "id": "58577",
                                  "title": "Lorem Ipsum",
                                  "lead": "lead / preview text",
                                  "imageUrl": "https://emarsys.com/app/uploads/2025/01/SAP_Emarsys_R_grad_blu.svg",
                                  "defaultAction": {
                                    "type": "OpenExternalUrl",
                                    "reporting": "{\"foo\": \"bar\"}",
                                    "url": "https://example.com"
                                  },
                                  "actions": [
                                    {
                                        "title": "anything",
                                        "type": "OpenExternalUrl",
                                        "reporting": "{\"foo\": \"bar\"}",
                                        "url": "https://example.com"
                                    }
                                  ],
                                  "tags": [
                                    "FOO",
                                    "BAR",
                                    "PINNED"
                                  ],
                                  "categoryIds": [
                                    291
                                  ],
                                  "receivedAt": 1737936858,
                                  "expiresAt": 1737936860,
                                  "properties": {
                                    "additionalProp1": "string",
                                    "additionalProp2": "string",
                                    "additionalProp3": "string"
                                  },
                                  "trackingInfo": "{\"campaignId\":\"100160594\",\"hiddenTags\":[\"NOT_SHOWN_JUST_FOR_REPORTING\"],\"treatments\":{}}"
                                }
                              ]
                            }"""
}