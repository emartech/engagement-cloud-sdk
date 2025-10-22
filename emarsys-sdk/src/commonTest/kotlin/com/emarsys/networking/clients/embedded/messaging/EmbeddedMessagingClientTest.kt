package com.emarsys.networking.clients.embedded.messaging

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.SdkException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.providers.InstantProvider
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embedded.messages.EmbeddedMessagingRequestFactoryApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
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
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class EmbeddedMessagingClientTest {
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var mockEmbeddedMessagesRequestFactory: EmbeddedMessagingRequestFactoryApi
    private lateinit var mockEmarsysNetworkClient: NetworkClientApi
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockSdkContext: SdkContextApi

    private companion object {
        val NOW = Clock.System.now()
        val IN_THROTTLING_LIMIT = NOW.plus(2.seconds)
        val OVER_THROTTLING_LIMIT = NOW.plus(6.seconds)
        val MORE_OVER_THROTTLING_LIMIT = NOW.plus(7.seconds)
    }

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkEventManager = mock()
        mockEventsDao = mock()
        mockEmbeddedMessagesRequestFactory = mock()
        mockClientExceptionHandler = mock()
        mockEmarsysNetworkClient = mock()
        mockTimestampProvider = mock()
        everySuspend { mockTimestampProvider.provide() } returns NOW
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.embeddedMessagingFrequencyCapSeconds } returns 5

        onlineEvents = MutableSharedFlow()

        everySuspend {
            mockSdkLogger.debug(any<String>())
        }
        everySuspend { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
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
            clientExceptionHandler = mockClientExceptionHandler,
            timestampProvider = mockTimestampProvider,
            sdkContext = mockSdkContext
        )

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

            everySuspend { mockEmarsysNetworkClient.send(request) } returns Result.success(
                expectedResponse
            )
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
            verifySuspend { mockEmarsysNetworkClient.send(request) }
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
    fun testConsumer_should_use_5s_throttling_for_fetchMessagesRequest() =
        runTest {
            createEmbeddedMessagingClient(backgroundScope).register()
            val event = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                id = "TEST_ID_1",
                nackCount = 0,
                offset = 0,
                categoryIds = listOf(1, 2, 3)
            )
            val event2 = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                id = "TEST_ID_2",
                nackCount = 0,
                offset = 0,
                categoryIds = listOf(1, 2, 3)
            )
            val request = UrlRequest(
                url = Url("https://test.com"),
                method = HttpMethod.Get
            )
            val request2 = request.copy(url = Url("https://test2.com"))
            every { mockEmbeddedMessagesRequestFactory.create(event) } returns request
            every { mockEmbeddedMessagesRequestFactory.create(event2) } returns request2
            val event1Response = Response(
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val event2noContentResponse = Response(
                status = HttpStatusCode.NoContent,
                headers = Headers.Empty,
                originalRequest = request2,
                bodyAsText = ""
            )
            val event2okResponse = Response(
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                originalRequest = request2,
                bodyAsText = "{}"
            )
            everySuspend { mockEmarsysNetworkClient.send(request) } returns Result.success(
                event1Response
            )
            everySuspend { mockEmarsysNetworkClient.send(request2) } returns Result.success(
                event2okResponse
            )
            everySuspend { mockEventsDao.removeEvent(event) } returns Unit
            everySuspend { mockEventsDao.removeEvent(event2) } returns Unit
            everySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event.id,
                        Result.success(event1Response)
                    )
                )
            } returns Unit
            everySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event2.id,
                        Result.success(event2noContentResponse)
                    )
                )
            } returns Unit
            everySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event2.id,
                        Result.success(event2okResponse)
                    )
                )
            } returns Unit
            everySuspend { mockTimestampProvider.provide() } sequentiallyReturns listOf(
                NOW,
                IN_THROTTLING_LIMIT,
                OVER_THROTTLING_LIMIT,
                MORE_OVER_THROTTLING_LIMIT
            )
            val onlineSdkEvents = backgroundScope.async {
                onlineEvents.take(3).toList()
            }

            onlineEvents.emit(event)
            onlineEvents.emit(event2)
            onlineEvents.emit(event2)

            advanceUntilIdle()

            onlineSdkEvents.await() shouldNotBe null
            verifySuspend { mockEmbeddedMessagesRequestFactory.create(event) }
            verifySuspend(VerifyMode.exactly(2)) { mockEmbeddedMessagesRequestFactory.create(event2) }
            verifySuspend { mockEmarsysNetworkClient.send(request) }
            verifySuspend(VerifyMode.exactly(1)) { mockEmarsysNetworkClient.send(request2) }
            verifySuspend { mockEventsDao.removeEvent(event) }
            verifySuspend(VerifyMode.exactly(2)) { mockEventsDao.removeEvent(event2) }
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event.id,
                        Result.success(event1Response)
                    )
                )
            }
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event2.id,
                        Result.success(event2noContentResponse)
                    )
                )
            }
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        event2.id,
                        Result.success(event2okResponse)
                    )
                )
            }
        }

    @Test
    fun testConsumer_should_consume_only_EmbeddedMessaging_events() = runTest {
        createEmbeddedMessagingClient(backgroundScope).register()
        val event1 = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)
        val event2 =
            SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                nackCount = 0,
                offset = 0,
                categoryIds = emptyList()
            )
        val event3 = SdkEvent.Internal.EmbeddedMessaging.FetchMeta(nackCount = 0)
        val event4 = SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages(
            nackCount = 0,
            updateData = emptyList()
        )
        val event5 = SdkEvent.Internal.EmbeddedMessaging.FetchNextPage(
            nackCount = 0,
            offset = 0,
            categoryIds = emptyList()
        )
        val wrongEvent =
            SdkEvent.Internal.Sdk.LinkContact(
                contactFieldValue = "value",
                nackCount = 0
            )
        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(6).toList()
        }
        val request = UrlRequest(
            url = Url("https://test.com"),
            method = HttpMethod.Get
        )

        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit
        everySuspend { mockEmbeddedMessagesRequestFactory.create(any()) } returns request
        everySuspend { mockEmarsysNetworkClient.send(any()) } returns Result.success(
            Response(
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
        )

        onlineEvents.emit(event1)
        onlineEvents.emit(event2)
        onlineEvents.emit(event3)
        onlineEvents.emit(event4)
        onlineEvents.emit(event5)
        onlineEvents.emit(wrongEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(event1, event2, event3, event4, event5, wrongEvent)
        verifySuspend(VerifyMode.exactly(5)) { mockEmbeddedMessagesRequestFactory.create(any()) }
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

        everySuspend { mockEmarsysNetworkClient.send(request) } returns Result.failure(
            SdkException.NetworkIOException(
                "Test Network error"
            )
        )

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit

        onlineEvents.emit(event)
        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(event)
        verifySuspend { mockEmbeddedMessagesRequestFactory.create(event) }
        verifySuspend { mockEmarsysNetworkClient.send(request) }
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
        verifySuspend(VerifyMode.exactly(0)) { mockEmarsysNetworkClient.send(any()) }
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