package com.emarsys.networking.clients.embedded.messaging

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embedded.messages.EmbeddedMessagingRequestFactoryApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import dev.mokkery.*
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.matcher.any
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmbeddedMessagingClientTest {
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var embeddedMessagingClient: EmbeddedMessagingClient
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var mockEmbeddedMessagesRequestFactory: EmbeddedMessagingRequestFactoryApi
    private lateinit var mockEmarsysNetworkClient: NetworkClientApi

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkEventManager = mock()
        mockEventsDao = mock()
        mockEmbeddedMessagesRequestFactory = mock()
        mockClientExceptionHandler = mock()
        mockEmarsysNetworkClient = mock()

        onlineEvents = MutableSharedFlow()

        everySuspend {
            mockSdkLogger.debug(any<String>())
        }
        everySuspend { mockSdkEventManager.onlineSdkEvents } returns onlineEvents

        embeddedMessagingClient = EmbeddedMessagingClient(
            sdkLogger = mockSdkLogger,
            sdkEventManager = mockSdkEventManager,
            applicationScope = backgroundScope,
            embeddedMessagingRequestFactory = mockEmbeddedMessagesRequestFactory,
            emarsysNetworkClient = mockEmarsysNetworkClient,
            eventsDao = mockEventsDao,
            clientExceptionHandler = mockClientExceptionHandler
        )
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    private fun createEmbeddedMessagingClient(applicationScope: CoroutineScope) =
        EmbeddedMessagingClient(
            sdkLogger = mockSdkLogger,
            sdkEventManager = mockSdkEventManager,
            applicationScope = applicationScope,
            embeddedMessagingRequestFactory = mockEmbeddedMessagesRequestFactory,
            emarsysNetworkClient = mockEmarsysNetworkClient,
            eventsDao = mockEventsDao,
            clientExceptionHandler = mockClientExceptionHandler
        )

    @Test
    fun testRegister_should_log_event() = runTest {
        embeddedMessagingClient.register()

        verifySuspend(VerifyMode.exactly(1)) {
            mockSdkLogger.debug(any<String>())
        }
    }

    @Test
    fun testConsumer_should_get_FetchBadgeCountEvent_from_flow_and_send_http_request_and_ack_event_and_emit_response() =
        runTest {
            createEmbeddedMessagingClient(backgroundScope).register()
            val event = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)

            val request = UrlRequest(
                url = Url("https://test.com"),
                method = HttpMethod.Get
            )

            every {
                mockEmbeddedMessagesRequestFactory.create(
                    event
                )
            } returns request

            val expectedResponse = Response(
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )

            everySuspend { mockEmarsysNetworkClient.send(request, any()) } returns expectedResponse
            everySuspend { mockEventsDao.removeEvent(event) } returns Unit
            everySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event.id,
                        Result.success(expectedResponse)
                    )
                )
            } returns Unit

            val onlineSdkEvents = backgroundScope.async {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(event)
            
            advanceUntilIdle()

            onlineSdkEvents.await() shouldNotBe null
            verifySuspend {
                mockEmbeddedMessagesRequestFactory.create(
                    event
                )
            }
            verifySuspend { mockEmarsysNetworkClient.send(request, any()) }
            verifySuspend { mockEventsDao.removeEvent(event) }
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event.id,
                        Result.success(expectedResponse)
                    )
                )
            }
        }

    @Test
    fun testConsumer_should_consume_only_EmbeddedMessaging_events() = runTest {
        createEmbeddedMessagingClient(backgroundScope).register()
        val event1 = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)
        val event2 =
            SdkEvent.Internal.EmbeddedMessaging.FetchMessages(nackCount = 0, offset = 0, categoryIds = emptyList())
        val event3 = SdkEvent.Internal.EmbeddedMessaging.FetchMeta(nackCount = 0)
        val event4 = SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages(nackCount = 0, updateData = emptyList())
        val wrongEvent =
            SdkEvent.Internal.Sdk.LinkContact(contactFieldId = 0, contactFieldValue = "value", nackCount = 0)
        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(5).toList()
        }
        val request = UrlRequest(
            url = Url("https://test.com"),
            method = HttpMethod.Get
        )

        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit
        everySuspend { mockEmbeddedMessagesRequestFactory.create(any()) } returns request
        everySuspend { mockEmarsysNetworkClient.send(any(), any()) } returns Response(
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            originalRequest = request,
            bodyAsText = "{}"
        )

        onlineEvents.emit(event1)
        onlineEvents.emit(event2)
        onlineEvents.emit(event3)
        onlineEvents.emit(event4)
        onlineEvents.emit(wrongEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(event1, event2, event3, event4, wrongEvent)
        verifySuspend(VerifyMode.exactly(4)) { mockEmbeddedMessagesRequestFactory.create(any()) }
    }

    @Test
    fun testConsumer_should_reEmit_events_into_flow_when_there_is_a_network_error() = runTest {
        createEmbeddedMessagingClient(backgroundScope).register()
        val event = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)
        val request = UrlRequest(
            url = Url("https://test.com"),
            method = HttpMethod.Get
        )

        every {
            mockEmbeddedMessagesRequestFactory.create(
                event
            )
        } returns request

        val expectedResponse = Response(
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            originalRequest = request,
            bodyAsText = "{}"
        )

        everySuspend { mockEmarsysNetworkClient.send(request, any()) } calls { args ->
            (args.arg(1) as (suspend () -> Unit)).invoke()
            expectedResponse
        }

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit

        onlineEvents.emit(event)
        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(event)
        verifySuspend { mockEmbeddedMessagesRequestFactory.create(event) }
        verifySuspend { mockEmarsysNetworkClient.send(request, any()) }
        verifySuspend { mockSdkEventManager.emitEvent(event) }
    }

    @Test
    fun testConsumer_should_call_clientExceptionHandler_when_exception_happens() = runTest {
        createEmbeddedMessagingClient(backgroundScope).register()
        val testException = Exception("Test Exception")
        val event = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)

        everySuspend { mockEmbeddedMessagesRequestFactory.create(event) } throws testException
        everySuspend {
            mockClientExceptionHandler.handleException(testException, any(), event)
        } returns Unit
        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }
        everySuspend {
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    event.id,
                    Result.failure<Exception>(testException)
                )
            )
        } returns Unit

        onlineEvents.emit(event)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(event)
        verifySuspend(VerifyMode.exactly(0)) { mockEmarsysNetworkClient.send(any(), any()) }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                any(),
                event
            )
        }
        verifySuspend {
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    event.id,
                    Result.failure<Exception>(testException)
                )
            )
        }
    }
}