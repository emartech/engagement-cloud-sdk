package com.sap.ec.networking.clients.deeplink

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException.NetworkIOException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.UserAgentProvider
import com.sap.ec.core.networking.UserAgentProviderApi
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.networking.clients.deepLink.DeepLinkClient
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import com.sap.ec.util.JsonUtil
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
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class DeepLinkClientTests {
    private companion object {
        const val TRACKING_ID = "trackingId"
        const val TEST_USER_AGENT = "userAgent"
        val TEST_BASE_URL = Url("https://test-base-url/")
    }

    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var mockNetworkClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockLogger: Logger
    private lateinit var mockUserAgentProvider: UserAgentProviderApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var json: Json
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var deepLinkClient: DeepLinkClient

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        sdkDispatcher = StandardTestDispatcher()
        mockNetworkClient = mock()
        mockUrlFactory = mock()
        mockLogger = mock(MockMode.autofill)
        mockUserAgentProvider = mock(MockMode.autofill)
        mockEventsDao = mock()
        mockClientExceptionHandler = mock(MockMode.autofill)
        json = JsonUtil.json
        mockSdkEventManager = mock(MockMode.autofill)
        onlineEvents = MutableSharedFlow()
        everySuspend { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
        everySuspend { mockUserAgentProvider.provide() } returns TEST_USER_AGENT
        every { mockUrlFactory.create(ECUrlType.DeepLink) } returns TEST_BASE_URL
        everySuspend { mockLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
        }
    }

    private fun createDeepLinkClient(applicationScope: CoroutineScope) =
        DeepLinkClient(
            mockNetworkClient,
            mockClientExceptionHandler,
            mockSdkEventManager,
            mockUrlFactory,
            mockUserAgentProvider,
            mockEventsDao,
            json,
            mockLogger,
            applicationScope
        )

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    @Test
    fun testConsumer_should_call_client_with_trackDeepLink_request_and_ack_event() = runTest {
        deepLinkClient = createDeepLinkClient(TestScope(sdkDispatcher))
        deepLinkClient.register()

        val expectedRequest = UrlRequest(
            TEST_BASE_URL,
            method = HttpMethod.Post,
            headers = mapOf(UserAgentProvider.USER_AGENT_HEADER_NAME to TEST_USER_AGENT),
            bodyString = json.encodeToString(buildJsonObject { put("ems_dl", TRACKING_ID) })
        )
        val response = Response(
            UrlRequest(TEST_BASE_URL, HttpMethod.Post),
            HttpStatusCode.OK,
            Headers.Empty,
            bodyAsText = "{}"
        )
        everySuspend { mockNetworkClient.send(expectedRequest) } returns Result.success(response)
        val trackDeepLink = SdkEvent.Internal.Sdk.TrackDeepLink(
            id = "trackDeepLink",
            trackingId = TRACKING_ID
        )
        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(trackDeepLink)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(trackDeepLink)
        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockNetworkClient.send(expectedRequest) }
        verifySuspend { mockEventsDao.removeEvent(trackDeepLink) }
        verifySuspend {
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = trackDeepLink.id,
                    Result.success(response)
                )
            )
        }
    }

    @Test
    fun testConsumer_shouldReEmitEvent_throughSdkEventDistributor_inCaseOfNetworkError() = runTest {
        deepLinkClient = createDeepLinkClient(backgroundScope)
        deepLinkClient.register()
        val testException = NetworkIOException("No Internet")

        everySuspend { mockNetworkClient.send(any()) } returns Result.failure(testException)
        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit

        val trackDeepLink = SdkEvent.Internal.Sdk.TrackDeepLink(
            id = "trackDeepLink",
            trackingId = TRACKING_ID
        )
        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(trackDeepLink)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(trackDeepLink)
        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockNetworkClient.send(any()) }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                any(),
                trackDeepLink
            )
        }
        verifySuspend {
            mockSdkEventManager.emitEvent(
                trackDeepLink
            )
        }
        verifySuspend(VerifyMode.exactly(0)) {
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = trackDeepLink.id,
                    Result.success(Unit)
                )
            )
        }
    }

    @Test
    fun testConsumer_shouldNotAckEvent_inCaseOfUnknownException() = runTest {
        deepLinkClient = createDeepLinkClient(backgroundScope)
        deepLinkClient.register()

        everySuspend { mockNetworkClient.send(any()) } throws Exception("Request error")
        val trackDeepLink = SdkEvent.Internal.Sdk.TrackDeepLink(
            id = "trackDeepLink",
            trackingId = TRACKING_ID
        )
        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(trackDeepLink)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(trackDeepLink)
        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockNetworkClient.send(any()) }
        verifySuspend(VerifyMode.exactly(0)) {
            mockSdkEventManager.emitEvent(trackDeepLink)
            mockEventsDao.removeEvent(trackDeepLink)
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = trackDeepLink.id,
                    Result.success(Unit)
                )
            )
        }
    }

    @Test
    fun testConsumer_should_call_clientExceptionHandler_when_exception_happens() = runTest {
        createDeepLinkClient(backgroundScope).register()
        val testException = Exception("Test exception")
        every { mockUrlFactory.create(ECUrlType.DeepLink) } throws testException
        val trackDeepLink = SdkEvent.Internal.Sdk.TrackDeepLink(
            id = "trackDeepLink",
            trackingId = TRACKING_ID
        )

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(trackDeepLink)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(trackDeepLink)
        verifySuspend(VerifyMode.exactly(0)) {
            mockNetworkClient.send(any())
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = trackDeepLink.id,
                    Result.success(Unit)
                )
            )
        }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                "DeepLinkClient - trackDeepLink(trackId: $TRACKING_ID)",
                trackDeepLink
            )
        }
    }
}
