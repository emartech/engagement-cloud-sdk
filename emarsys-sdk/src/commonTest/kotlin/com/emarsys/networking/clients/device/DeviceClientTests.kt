package com.emarsys.networking.clients.device

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
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
import io.ktor.http.headers
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
import kotlinx.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceClientTests {
    private companion object {
        const val DEVICE_INFO_STRING = "testDeviceInfo"
        val URL = Url("https://www.testUrl.com/testAppCode/client")
        val TIMESTAMP = Clock.System.now()
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var mockContactTokenHandler: ContactTokenHandlerApi
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEmarsysClient = mock()
        mockUrlFactory = mock()
        every { mockUrlFactory.create(REGISTER_DEVICE_INFO) } returns URL
        mockDeviceInfoCollector = mock()
        everySuspend { mockDeviceInfoCollector.collect() } returns DEVICE_INFO_STRING
        mockContactTokenHandler = mock()
        mockSdkLogger = mock(MockMode.autofill)
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
        }
        onlineEvents = spy(MutableSharedFlow(replay = 5))
        mockSdkEventManager = mock(MockMode.autofill)
        every { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
        mockEventsDao = mock(MockMode.autoUnit)
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    private fun createDeviceClient(applicationScope: CoroutineScope): DeviceClient {
        return DeviceClient(
            mockEmarsysClient,
            mockUrlFactory,
            mockDeviceInfoCollector,
            mockContactTokenHandler,
            mockSdkEventManager,
            mockEventsDao,
            applicationScope,
            mockSdkLogger,
        )
    }

    @Test
    fun testConsumer_should_send_deviceInfo_to_client_service() = runTest {
        createDeviceClient(backgroundScope).register()

        val testRefreshToken = "testRefreshToken"
        val testContactToken = "testContactToken"
        val request = UrlRequest(
            URL,
            HttpMethod.Post,
            DEVICE_INFO_STRING
        )
        val expectedResponse = Response(
            request,
            HttpStatusCode.OK,
            headers {
                append("Content-Type", "application/json")
                append("X-Client-State", "testClientState")
            },
            """{"refreshToken":"$testRefreshToken", "contactToken":"$testContactToken"}"""
        )
        everySuspend { mockEmarsysClient.send(any(), any()) } returns expectedResponse
        everySuspend { mockContactTokenHandler.handleContactTokens(expectedResponse) } returns Unit
        val registerDeviceInfoEvent = SdkEvent.Internal.Sdk.RegisterDeviceInfo()

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }


        onlineEvents.emit(registerDeviceInfoEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(registerDeviceInfoEvent)
        verifySuspend {
            mockDeviceInfoCollector.collect()
            mockUrlFactory.create(REGISTER_DEVICE_INFO)
            mockEmarsysClient.send(request, any())
            mockContactTokenHandler.handleContactTokens(expectedResponse)
            mockEventsDao.removeEvent(registerDeviceInfoEvent)
        }
    }

    @Test
    fun testConsumer_should_not_call_contactTokenHandler_when_response_is_204() = runTest {
        createDeviceClient(backgroundScope).register()

        val testUrl = Url("https://www.testUrl.com/testAppCode/client")
        val testDeviceInfoString = "testDeviceInfo"
        val request = UrlRequest(
            testUrl,
            HttpMethod.Post,
            testDeviceInfoString
        )
        val expectedResponse = Response(
            request,
            HttpStatusCode.NoContent,
            headers {
                append("Content-Type", "application/json")
                append("X-Client-State", "testClientState")
            },
            ""
        )
        every { mockUrlFactory.create(REGISTER_DEVICE_INFO) } returns testUrl
        everySuspend { mockEmarsysClient.send(any(), any()) } returns expectedResponse
        val registerDeviceInfoEvent = SdkEvent.Internal.Sdk.RegisterDeviceInfo()

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(registerDeviceInfoEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(registerDeviceInfoEvent)
        verifySuspend {
            mockDeviceInfoCollector.collect()
            mockUrlFactory.create(REGISTER_DEVICE_INFO)
            mockEmarsysClient.send(request, any())
            mockEventsDao.removeEvent(registerDeviceInfoEvent)
        }
        verifySuspend(VerifyMode.exactly(0)) {
            mockContactTokenHandler.handleContactTokens(
                expectedResponse
            )
        }
    }

    @Test
    fun testConsumer_should_reEmit_events_on_network_error() = runTest {
        createDeviceClient(backgroundScope).register()

        everySuspend { mockEmarsysClient.send(any(), any()) } calls { args ->
            (args.arg(1) as suspend () -> Unit).invoke()
            throw IOException("No Internet")
        }
        val registerDeviceInfoEvent = SdkEvent.Internal.Sdk.RegisterDeviceInfo()
        everySuspend { mockSdkEventManager.emitEvent(registerDeviceInfoEvent) } returns Unit

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(registerDeviceInfoEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(registerDeviceInfoEvent)
        verifySuspend {
            mockDeviceInfoCollector.collect()
            mockUrlFactory.create(REGISTER_DEVICE_INFO)
            mockEmarsysClient.send(any(), any())
            mockSdkLogger.error(any(), any<Throwable>())
            mockSdkEventManager.emitEvent(registerDeviceInfoEvent)
        }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(registerDeviceInfoEvent) }
    }

    @Test
    fun testConsumer_should_not_ack_on_unknown_exception() = runTest {
        createDeviceClient(backgroundScope).register()

        every { mockUrlFactory.create(any()) } throws RuntimeException("test")
        val registerDeviceInfoEvent = SdkEvent.Internal.Sdk.RegisterDeviceInfo()
        everySuspend { mockSdkEventManager.emitEvent(registerDeviceInfoEvent) } returns Unit

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(registerDeviceInfoEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(registerDeviceInfoEvent)
        verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any(), any()) }
        verifySuspend { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(registerDeviceInfoEvent) }
    }


    @Test
    fun testConsumer_should_ack_event_when_known_exception_happens() = forAll(
        table(
            headers("exception"),
            listOf(
                row(
                    FailedRequestException(
                        Response(
                            UrlRequest(URL, HttpMethod.Post),
                            HttpStatusCode.OK,
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
            createDeviceClient(backgroundScope).register()

            every {
                mockUrlFactory.create(REGISTER_DEVICE_INFO)
            } throws testException
            val registerDeviceInfoEvent =
                SdkEvent.Internal.Sdk.RegisterDeviceInfo("testId", null, TIMESTAMP)

            val onlineSdkEvents = backgroundScope.async {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(registerDeviceInfoEvent)

            advanceUntilIdle()

            onlineSdkEvents.await() shouldBe listOf(registerDeviceInfoEvent)
            verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any(), any()) }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
            verifySuspend { mockEventsDao.removeEvent(registerDeviceInfoEvent) }
        }
    }
}