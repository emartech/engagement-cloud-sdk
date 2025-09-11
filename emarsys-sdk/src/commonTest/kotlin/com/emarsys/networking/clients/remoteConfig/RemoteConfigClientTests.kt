package com.emarsys.networking.clients.remoteConfig

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.crypto.CryptoApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.SdkException
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.error.ClientExceptionHandler
import com.emarsys.remoteConfig.RemoteConfigResponse
import com.emarsys.remoteConfig.RemoteConfigResponseHandlerApi
import com.emarsys.util.JsonUtil
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class RemoteConfigClientTests {
    private lateinit var mockNetworkClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockCrypto: CryptoApi
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockRemoteConfigResponseHandler: RemoteConfigResponseHandlerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var mockSdkLogger: Logger
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>

    private companion object {
        const val CONFIG_RESULT = """{"logLevel":"ERROR"}"""
        const val CONFIG_SIGNATURE_RESULT = """<<<testSignature>>>"""
        val configUrl = Url("testRemoteConfigUrl")
        val configSignatureUrl = Url("testRemoteConfigSignatureUrl")
        val configRequest = UrlRequest(configUrl, HttpMethod.Get)
        val configSignatureRequest = UrlRequest(configSignatureUrl, HttpMethod.Get)
        const val EVENT_ID = "testId"
        val timestamp = Clock.System.now()
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockNetworkClient = mock(MockMode.autofill)
        mockUrlFactory = mock()
        mockCrypto = mock()
        mockSdkEventManager = mock()
        onlineEvents = spy(MutableSharedFlow(replay = 5))
        every { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit
        mockRemoteConfigResponseHandler = mock(MockMode.autoUnit)
        mockEventsDao = mock(MockMode.autoUnit)
        mockClientExceptionHandler = mock(MockMode.autoUnit)
        mockSdkLogger = mock(MockMode.autofill)
        everySuspend { mockSdkLogger.error(any<String>(), any<Throwable>()) } returns Unit
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
        resetCalls()
        resetAnswers()
    }

    private fun createClient(applicationsScope: CoroutineScope): RemoteConfigClient {
        return RemoteConfigClient(
            mockNetworkClient,
            mockClientExceptionHandler,
            mockUrlFactory,
            mockSdkEventManager,
            mockRemoteConfigResponseHandler,
            applicationsScope,
            mockEventsDao,
            mockCrypto,
            JsonUtil.json,
            mockSdkLogger
        )
    }

    @Test
    fun testConsumer_shouldCall_responseHandler_withAppCodeBasedRemoteConfig() = runTest {
        createClient(backgroundScope).register()

        val configResponse =
            Response(configRequest, HttpStatusCode.OK, Headers.Empty, CONFIG_RESULT)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            CONFIG_SIGNATURE_RESULT
        )
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every {
            mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE)
        } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns Result.success(configResponse)
        everySuspend {
            mockNetworkClient.send(
                configSignatureRequest
            )
        } returns Result.success(configSignatureResponse)
        everySuspend { mockCrypto.verify(any(), any()) } returns true

        val appCodeBasedRemoteConfigEvent = SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig()

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(appCodeBasedRemoteConfigEvent)

        onlineSdkEvents.await() shouldBe listOf(appCodeBasedRemoteConfigEvent)
        verifySuspend { mockNetworkClient.send(configRequest) }
        verifySuspend { mockNetworkClient.send(configSignatureRequest) }
        verifySuspend { mockRemoteConfigResponseHandler.handle(RemoteConfigResponse(logLevel = LogLevel.Error)) }
        verifySuspend { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) }
        verifySuspend { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) }
        verifySuspend { mockEventsDao.removeEvent(appCodeBasedRemoteConfigEvent) }
    }

    @Test
    fun testConsumer_shouldCall_responseHandler_withGlobalRemoteConfig() = runTest {
        createClient(backgroundScope).register()

        val configResponse =
            Response(configRequest, HttpStatusCode.OK, Headers.Empty, CONFIG_RESULT)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            CONFIG_SIGNATURE_RESULT
        )

        every { mockUrlFactory.create(EmarsysUrlType.GLOBAL_REMOTE_CONFIG) } returns configUrl
        every {
            mockUrlFactory.create(EmarsysUrlType.GLOBAL_REMOTE_CONFIG_SIGNATURE)
        } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns Result.success(configResponse)
        everySuspend {
            mockNetworkClient.send(
                configSignatureRequest
            )
        } returns Result.success(configSignatureResponse)
        everySuspend { mockCrypto.verify(any(), any()) } returns true

        val globalRemoteConfig = SdkEvent.Internal.Sdk.ApplyGlobalRemoteConfig()

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(globalRemoteConfig)

        onlineSdkEvents.await() shouldBe listOf(globalRemoteConfig)
        verifySuspend { mockNetworkClient.send(configRequest) }
        verifySuspend { mockNetworkClient.send(configSignatureRequest) }
        verifySuspend { mockRemoteConfigResponseHandler.handle(RemoteConfigResponse(logLevel = LogLevel.Error)) }
        verifySuspend { mockUrlFactory.create(EmarsysUrlType.GLOBAL_REMOTE_CONFIG) }
        verifySuspend { mockUrlFactory.create(EmarsysUrlType.GLOBAL_REMOTE_CONFIG_SIGNATURE) }
        verifySuspend { mockEventsDao.removeEvent(globalRemoteConfig) }
    }

    @Test
    fun testConsumer_shouldNotCall_responseHandler_butAckEvent_whenVerification_fails() = runTest {
        createClient(backgroundScope).register()

        val configResponse =
            Response(configRequest, HttpStatusCode.OK, Headers.Empty, CONFIG_RESULT)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            CONFIG_SIGNATURE_RESULT
        )

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every {
            mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE)
        } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns Result.success(configResponse)
        everySuspend {
            mockNetworkClient.send(
                configSignatureRequest
            )
        } returns Result.success(configSignatureResponse)
        everySuspend { mockCrypto.verify(any(), any()) } returns false


        val appCodeBasedRemoteConfigEvent = SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig()

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(appCodeBasedRemoteConfigEvent)

        onlineSdkEvents.await() shouldBe listOf(appCodeBasedRemoteConfigEvent)
        verifySuspend(VerifyMode.exactly(0)) { mockRemoteConfigResponseHandler.handle(any()) }
        verifySuspend { mockEventsDao.removeEvent(appCodeBasedRemoteConfigEvent) }
    }

    @Test
    fun testConsumer_shouldNotCall_responseHandler_butAckEvent_whenConfigIsNotFound() = runTest {
        createClient(backgroundScope).register()

        val configResponse =
            Response(configRequest, HttpStatusCode.NotFound, Headers.Empty, CONFIG_RESULT)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            CONFIG_SIGNATURE_RESULT
        )

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every {
            mockUrlFactory.create(
                EmarsysUrlType.REMOTE_CONFIG_SIGNATURE
            )
        } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns Result.failure(
            SdkException.FailedRequestException(
                configResponse
            )
        )
        everySuspend {
            mockNetworkClient.send(
                configSignatureRequest
            )
        } returns Result.success(configSignatureResponse)

        val appCodeBasedRemoteConfigEvent = SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig()

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(appCodeBasedRemoteConfigEvent)

        onlineSdkEvents.await() shouldBe listOf(appCodeBasedRemoteConfigEvent)
        verifySuspend(VerifyMode.exactly(0)) { mockRemoteConfigResponseHandler.handle(any()) }
        verifySuspend { mockEventsDao.removeEvent(appCodeBasedRemoteConfigEvent) }
    }

    @Test
    fun testConsumer_shouldNotCall_responseHandler_butAckEvent_whenSignatureIsNotFound() = runTest {
        createClient(backgroundScope).register()

        val configResponse =
            Response(configRequest, HttpStatusCode.OK, Headers.Empty, CONFIG_RESULT)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.NotFound,
            Headers.Empty,
            CONFIG_SIGNATURE_RESULT
        )

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every {
            mockUrlFactory.create(
                EmarsysUrlType.REMOTE_CONFIG_SIGNATURE
            )
        } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns Result.success(configResponse)
        everySuspend {
            mockNetworkClient.send(
                configSignatureRequest
            )
        } returns Result.failure(SdkException.FailedRequestException(configSignatureResponse))

        val appCodeBasedRemoteConfigEvent = SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig()

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(appCodeBasedRemoteConfigEvent)

        onlineSdkEvents.await() shouldBe listOf(appCodeBasedRemoteConfigEvent)
        verifySuspend(VerifyMode.exactly(0)) { mockRemoteConfigResponseHandler.handle(any()) }
        verifySuspend { mockEventsDao.removeEvent(appCodeBasedRemoteConfigEvent) }
    }

    @Test
    fun testConsumer_shouldNotCall_responseHandler_andCallClientExceptionHandler_whenFetchingSignatureThrows() =
        runTest {
            createClient(backgroundScope).register()

            val testException = Exception("Test exception")
            val configResponse =
                Response(configRequest, HttpStatusCode.OK, Headers.Empty, CONFIG_RESULT)

            every {
                mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG)
            } returns configUrl
            everySuspend { mockNetworkClient.send(configRequest) } returns Result.success(configResponse)
            every {
                mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE)
            } returns configSignatureUrl
            everySuspend {
                mockNetworkClient.send(
                    configSignatureRequest
                )
            } throws testException

            val remoteConfigEvent = SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig(
                id = EVENT_ID,
                timestamp = timestamp
            )

            val onlineSdkEvents = backgroundScope.async {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(remoteConfigEvent)
            advanceUntilIdle()

            onlineSdkEvents.await() shouldBe listOf(remoteConfigEvent)
            verifySuspend(VerifyMode.exactly(0)) { mockCrypto.verify(any(), any()) }
            verifySuspend(VerifyMode.exactly(0)) { mockRemoteConfigResponseHandler.handle(any()) }
            verifySuspend {
                mockClientExceptionHandler.handleException(
                    any<Exception>(),
                    "RemoteConfigClient: ConsumeRemoteConfigEvents error",
                    remoteConfigEvent
                )
            }
        }

    @Test
    fun testConsumer_should_reEmit_events_on_network_error() = runTest {
        createClient(backgroundScope).register()
        val testException = IOException("No Internet")
        every {
            mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG)
        } returns configUrl
        every {
            mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE)
        } returns configSignatureUrl

        everySuspend { mockNetworkClient.send(any()) } returns Result.failure(testException)
        val remoteConfigEvent = SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig(
            id = EVENT_ID,
            timestamp = timestamp
        )
        everySuspend { mockSdkEventManager.emitEvent(remoteConfigEvent) } returns Unit
        everySuspend {
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = remoteConfigEvent.id,
                    Result.failure<Exception>(testException)
                )
            )
        } returns Unit

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(remoteConfigEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(remoteConfigEvent)
        verifySuspend {
            mockNetworkClient.send(any())
            mockSdkEventManager.emitEvent(remoteConfigEvent)
        }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                any<IOException>(),
                "RemoteConfigClient: ConsumeRemoteConfigEvents error",
                remoteConfigEvent
            )
        }
    }
}