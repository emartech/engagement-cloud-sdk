package com.emarsys.networking.clients.device

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.DeviceConstants.DEVICE_INFO_STORAGE_KEY
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.DeviceInfoUpdater
import com.emarsys.core.device.DeviceInfoUpdaterApi
import com.emarsys.core.exceptions.SdkException.NetworkIOException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.fake.FakeStringStorage
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
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
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class DeviceClientTests {
    private companion object {
        const val STORED_DEVICE_INFO_STRING = "testDeviceInfo"
        const val NEW_DEVICE_INFO = "newDeviceInfo"
        val URL = Url("https://www.testUrl.com/testAppCode/client")
        val TIMESTAMP = Clock.System.now()
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var mockContactTokenHandler: ContactTokenHandlerApi
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var mockSdkLogger: Logger
    private lateinit var deviceInfoUpdater: DeviceInfoUpdaterApi
    private lateinit var fakeStringStorage: FakeStringStorage
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEmarsysClient = mock()
        mockUrlFactory = mock()
        every { mockUrlFactory.create(EmarsysUrlType.RegisterDeviceInfo) } returns URL
        mockDeviceInfoCollector = mock()
        everySuspend { mockDeviceInfoCollector.collect() } returns NEW_DEVICE_INFO
        mockContactTokenHandler = mock()
        mockSdkLogger = mock(MockMode.autofill)
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
        }
        fakeStringStorage = FakeStringStorage()
        fakeStringStorage.put(
            DEVICE_INFO_STORAGE_KEY,
            STORED_DEVICE_INFO_STRING.hashCode().toString()
        )

        deviceInfoUpdater = DeviceInfoUpdater(fakeStringStorage)

        onlineEvents = MutableSharedFlow(replay = 5)
        mockSdkEventManager = mock(MockMode.autofill)
        every { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
        mockEventsDao = mock(MockMode.autofill)
        mockClientExceptionHandler = mock(MockMode.autoUnit)
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    private fun createDeviceClient(applicationScope: CoroutineScope): DeviceClient {
        return DeviceClient(
            mockEmarsysClient,
            mockClientExceptionHandler,
            mockUrlFactory,
            mockDeviceInfoCollector,
            deviceInfoUpdater,
            mockContactTokenHandler,
            mockSdkEventManager,
            mockEventsDao,
            applicationScope,
            mockSdkLogger
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
            NEW_DEVICE_INFO
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
        everySuspend { mockEmarsysClient.send(any()) } returns Result.success(expectedResponse)
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
            mockUrlFactory.create(EmarsysUrlType.RegisterDeviceInfo)
            mockEmarsysClient.send(request)
            mockContactTokenHandler.handleContactTokens(expectedResponse)
            mockEventsDao.removeEvent(registerDeviceInfoEvent)
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = registerDeviceInfoEvent.id,
                    Result.success(Unit)
                )
            )
        }
    }

    @Test
    fun testConsumer_should_not_call_contactTokenHandler_when_response_is_204() = runTest {
        createDeviceClient(backgroundScope).register()

        val testUrl = Url("https://www.testUrl.com/testAppCode/client")
        val request = UrlRequest(
            testUrl,
            HttpMethod.Post,
            NEW_DEVICE_INFO
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
        every { mockUrlFactory.create(EmarsysUrlType.RegisterDeviceInfo) } returns testUrl
        everySuspend { mockEmarsysClient.send(any()) } returns Result.success(expectedResponse)
        val registerDeviceInfoEvent = SdkEvent.Internal.Sdk.RegisterDeviceInfo()

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(registerDeviceInfoEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(registerDeviceInfoEvent)
        verifySuspend {
            mockDeviceInfoCollector.collect()
            mockUrlFactory.create(EmarsysUrlType.RegisterDeviceInfo)
            mockEmarsysClient.send(request)
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
        val testException = NetworkIOException("No Internet")

        everySuspend { mockEmarsysClient.send(any()) } returns Result.failure(testException)
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
            mockUrlFactory.create(EmarsysUrlType.RegisterDeviceInfo)
            mockEmarsysClient.send(any())
        }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                any(),
                registerDeviceInfoEvent
            )
        }
        verifySuspend {
            mockSdkEventManager.emitEvent(registerDeviceInfoEvent)
        }
    }

    @Test
    fun testConsumer_should_callClientExceptionHandler_when_exception_happens() = runTest {
        createDeviceClient(backgroundScope).register()
        val testException = Exception("Test Exception")

        every {
            mockUrlFactory.create(EmarsysUrlType.RegisterDeviceInfo)
        } throws testException

        val registerDeviceInfoEvent =
            SdkEvent.Internal.Sdk.RegisterDeviceInfo("testId", TIMESTAMP)

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(registerDeviceInfoEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(registerDeviceInfoEvent)
        verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any()) }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                "DeviceClient - consumeRegisterDeviceInfo",
                registerDeviceInfoEvent
            )
        }
    }

    @Test
    fun testConsumer_should_collectDeviceInfo_andAckTheEvent_ifDeviceInfoHasNotChanged() = runTest {
        everySuspend { mockDeviceInfoCollector.collect() } returns STORED_DEVICE_INFO_STRING

        createDeviceClient(backgroundScope).register()

        val registerDeviceInfoEvent = SdkEvent.Internal.Sdk.RegisterDeviceInfo("testId", TIMESTAMP)

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(registerDeviceInfoEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(registerDeviceInfoEvent)

        verifySuspend(VerifyMode.exactly(1)) { mockDeviceInfoCollector.collect() }
        verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any()) }
        verifySuspend(VerifyMode.exactly(1)) { mockEventsDao.removeEvent(registerDeviceInfoEvent) }
        verifySuspend { mockSdkLogger.debug("DeviceInfo has not changed.") }
        verifySuspend {
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = registerDeviceInfoEvent.id,
                    Result.success(Unit)
                )
            )
        }
    }
}
