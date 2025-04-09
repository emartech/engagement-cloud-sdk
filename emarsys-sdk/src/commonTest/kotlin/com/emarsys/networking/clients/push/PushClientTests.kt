package com.emarsys.networking.clients.push

import com.emarsys.api.push.PushConstants
import com.emarsys.context.DefaultUrlsApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.event.model.OnlineSdkEvent
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.spy
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


@OptIn(ExperimentalCoroutinesApi::class)
class PushClientTests {
    private companion object {
        const val TEST_PUSH_TOKEN = "testPushToken"
        const val ID = "testId"
        val URL = Url("https://www.testUrl.com/testAppCode/client")
        val TIMESTAMP = Clock.System.now()
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>

    @BeforeTest
    fun setUp() {
        mockEmarsysClient = mock()
        mockDefaultUrls = mock()
        mockUrlFactory = mock()
        every { mockUrlFactory.create(EmarsysUrlType.PUSH_TOKEN, null) } returns URL
        onlineEvents = spy(MutableSharedFlow(replay = 5))
        mockSdkEventManager = mock()
        every { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
        mockEventsDao = mock(MockMode.autoUnit)
        mockSdkLogger = mock(MockMode.autofill)
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
        }
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    private fun createPushClient(applicationScope: CoroutineScope): PushClient {
        return PushClient(
            emarsysClient = mockEmarsysClient,
            urlFactory = mockUrlFactory,
            sdkEventManager = mockSdkEventManager,
            applicationScope = applicationScope,
            eventsDao = mockEventsDao,
            sdkLogger = mockSdkLogger,
            json = Json { ignoreUnknownKeys = true }
        )
    }

    @Test
    fun testRegisterPushToken() = runTest {
        createPushClient(backgroundScope).register()

        val expectedUrlRequest = UrlRequest(
            URL,
            HttpMethod.Put,
            """{"pushToken":"testPushToken"}"""
        )
        everySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            ""
        )
        val registerPushTokenEvent = SdkEvent.Internal.Sdk.RegisterPushToken(
            ID,
            buildJsonObject {
                put(PushConstants.PUSH_TOKEN_KEY, JsonPrimitive(TEST_PUSH_TOKEN))
            },
            TIMESTAMP
        )

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(registerPushTokenEvent)

        onlineSdkEvents.await() shouldBe listOf(registerPushTokenEvent)
        verifySuspend {
            mockEmarsysClient.send(expectedUrlRequest, any())
        }
        verifySuspend { mockEventsDao.removeEvent(registerPushTokenEvent) }
    }

    @Test
    fun testClearPushToken() = runTest {
        createPushClient(backgroundScope).register()

        val expectedUrlRequest = UrlRequest(
            URL,
            HttpMethod.Delete
        )
        everySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            ""
        )
        val clearPushTokenEvent = SdkEvent.Internal.Sdk.ClearPushToken(ID, null, TIMESTAMP)

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(clearPushTokenEvent)

        onlineSdkEvents.await() shouldBe listOf(clearPushTokenEvent)
        verifySuspend {
            mockEmarsysClient.send(expectedUrlRequest, any())
        }
        verifySuspend { mockEventsDao.removeEvent(clearPushTokenEvent) }
    }


    @Test
    fun testConsumer_should_reEmit_events_on_network_error() = runTest {
        createPushClient(backgroundScope).register()

        everySuspend { mockEmarsysClient.send(any(), any()) } calls { args ->
            (args.arg(1) as suspend () -> Unit).invoke()
            throw IOException("No Internet")
        }
        val clearPushTokenEvent = SdkEvent.Internal.Sdk.ClearPushToken(ID, null, TIMESTAMP)
        everySuspend { mockSdkEventManager.emitEvent(clearPushTokenEvent) } returns Unit

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(clearPushTokenEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(clearPushTokenEvent)
        verifySuspend {
            mockEmarsysClient.send(any(), any())
            mockSdkLogger.error(any(), any<Throwable>())
            mockSdkEventManager.emitEvent(clearPushTokenEvent)
        }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(clearPushTokenEvent) }
    }

    @Test
    fun testConsumer_should_not_ack_on_unknown_exception() = runTest {
        createPushClient(backgroundScope).register()

        every { mockUrlFactory.create(any(), null) } throws RuntimeException("test")
        val clearPushTokenEvent = SdkEvent.Internal.Sdk.ClearPushToken(ID, null, TIMESTAMP)
        everySuspend { mockSdkEventManager.emitEvent(clearPushTokenEvent) } returns Unit

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(clearPushTokenEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(clearPushTokenEvent)
        verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any(), any()) }
        verifySuspend { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(clearPushTokenEvent) }
    }


    @Test
    fun testConsumer_should_ack_event_when_known_exception_happens() = forAll(
        table(
            headers("exception"),
            listOf(
                row(
                    FailedRequestException(
                        Response(
                            UrlRequest(URL, HttpMethod.Delete),
                            HttpStatusCode.BadRequest,
                            Headers.Empty,
                            bodyAsText = ""
                        ),
                    )
                ),
                row(RetryLimitReachedException("Retry limit reached")),
                row(MissingApplicationCodeException("Missing app code")),
            )
        )
    ) { testException ->
        runTest {
            createPushClient(backgroundScope).register()

            every {
                mockUrlFactory.create(EmarsysUrlType.PUSH_TOKEN, null)
            } throws testException
            val clearPushTokenEvent =
                SdkEvent.Internal.Sdk.ClearPushToken(ID, null, TIMESTAMP)

            val onlineSdkEvents = backgroundScope.async {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(clearPushTokenEvent)

            advanceUntilIdle()

            onlineSdkEvents.await() shouldBe listOf(clearPushTokenEvent)
            verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any(), any()) }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
            verifySuspend { mockEventsDao.removeEvent(clearPushTokenEvent) }
        }
    }
}