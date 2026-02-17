package com.sap.ec.networking.clients.push

import com.sap.ec.context.DefaultUrlsApi
import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.networking.clients.error.ClientExceptionHandler
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
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class PushClientTests {
    private companion object {
        const val TEST_PUSH_TOKEN = "testPushToken"
        const val TEST_APPLICATION_CODE = "testAppCode"
        const val ID = "testId"
        val URL = Url("https://www.testUrl.com/testAppCode/client")
        val TIMESTAMP = Clock.System.now()
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var mockSdkLogger: Logger
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>

    @BeforeTest
    fun setUp() {
        mockEmarsysClient = mock()
        mockDefaultUrls = mock()
        mockUrlFactory = mock()
        every { mockUrlFactory.create(ECUrlType.ClearPushToken, any()) } returns URL
        every { mockUrlFactory.create(ECUrlType.PushToken) } returns URL
        onlineEvents = spy(MutableSharedFlow(replay = 5))
        mockSdkEventManager = mock()
        every { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit
        mockEventsDao = mock(MockMode.autoUnit)
        mockClientExceptionHandler = mock(MockMode.autoUnit)
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
            ecClient = mockEmarsysClient,
            clientExceptionHandler = mockClientExceptionHandler,
            urlFactory = mockUrlFactory,
            sdkEventManager = mockSdkEventManager,
            applicationScope = applicationScope,
            eventsDao = mockEventsDao,
            json = Json { ignoreUnknownKeys = true },
            sdkLogger = mockSdkLogger
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
        everySuspend { mockEmarsysClient.send(expectedUrlRequest) } returns Result.success(
            Response(
                expectedUrlRequest,
                HttpStatusCode.OK,
                Headers.Empty,
                ""
            )
        )
        val registerPushTokenEvent = SdkEvent.Internal.Sdk.RegisterPushToken(
            id = ID,
            pushToken = TEST_PUSH_TOKEN,
            timestamp = TIMESTAMP
        )

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(registerPushTokenEvent)

        onlineSdkEvents.await() shouldBe listOf(registerPushTokenEvent)
        verifySuspend {
            mockEmarsysClient.send(expectedUrlRequest)
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
        val response = Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            ""
        )
        val result = Result.success(response)

        everySuspend { mockEmarsysClient.send(expectedUrlRequest) } returns result
        val clearPushTokenEvent = SdkEvent.Internal.Sdk.ClearPushToken(
            ID,
            TIMESTAMP,
            applicationCode = TEST_APPLICATION_CODE
        )

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(clearPushTokenEvent)

        onlineSdkEvents.await() shouldBe listOf(clearPushTokenEvent)
        verifySuspend {
            mockEmarsysClient.send(expectedUrlRequest)
        }
        verifySuspend {
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = ID,
                    result
                )
            )
        }
        verifySuspend { mockEventsDao.removeEvent(clearPushTokenEvent) }
    }

    @Test
    fun testConsumer_should_reEmit_events_on_network_error() = runTest {
        createPushClient(backgroundScope).register()
        val testException = IOException("Network error")

        everySuspend { mockEmarsysClient.send(any()) } returns Result.failure(testException)
        val clearPushTokenEvent = SdkEvent.Internal.Sdk.ClearPushToken(
            ID,
            TIMESTAMP,
            applicationCode = TEST_APPLICATION_CODE
        )
        everySuspend { mockSdkEventManager.emitEvent(clearPushTokenEvent) } returns Unit

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(clearPushTokenEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(clearPushTokenEvent)
        verifySuspend {
            mockEmarsysClient.send(any())
            mockSdkEventManager.emitEvent(clearPushTokenEvent)
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = clearPushTokenEvent.id,
                    Result.failure<Exception>(testException)
                )
            )
        }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                "PushClient - consumePushEvents",
                clearPushTokenEvent
            )
        }
    }

    @Test
    fun testConsumer_should_callClientExceptionHandler_when_exception_happens() = runTest {
        createPushClient(backgroundScope).register()

        val testException = Exception("Test exception")
        val clearPushTokenEvent =
            SdkEvent.Internal.Sdk.ClearPushToken(
                ID,
                TIMESTAMP,
                applicationCode = TEST_APPLICATION_CODE
            )
        every {
            mockUrlFactory.create(ECUrlType.ClearPushToken, clearPushTokenEvent)
        } throws testException

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(clearPushTokenEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(clearPushTokenEvent)
        verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any()) }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                "PushClient - consumePushEvents",
                clearPushTokenEvent
            )
        }
    }
}
