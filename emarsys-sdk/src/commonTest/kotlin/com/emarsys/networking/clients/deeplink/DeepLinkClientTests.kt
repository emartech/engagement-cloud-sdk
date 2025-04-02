package com.emarsys.networking.clients.deeplink

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.UserAgentProviderApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.deepLink.DeepLinkClient
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
    private lateinit var json: Json
    private lateinit var onlineEvents: MutableSharedFlow<SdkEvent>
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var deepLinkClient: DeepLinkClient

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        sdkDispatcher = StandardTestDispatcher()
        mockNetworkClient = mock()
        mockUrlFactory = mock()
        mockLogger = mock(MockMode.autofill)
        mockUserAgentProvider = mock(MockMode.autofill)
        json = JsonUtil.json
        mockSdkEventDistributor = mock()
        onlineEvents = MutableSharedFlow(replay = 5)
        everySuspend { mockSdkEventDistributor.onlineEvents } returns onlineEvents
        everySuspend { mockUserAgentProvider.provide() } returns TEST_USER_AGENT
        every { mockUrlFactory.create(EmarsysUrlType.DEEP_LINK, null) } returns TEST_BASE_URL
        everySuspend { mockLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
            throw it.args[1] as Throwable
        }

        deepLinkClient = DeepLinkClient(
            mockNetworkClient,
            mockSdkEventDistributor,
            mockUrlFactory,
            mockUserAgentProvider,
            json,
            mockLogger,
            sdkDispatcher
        )
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }


    @Test
    fun testConsumer_should_call_client_with_trackDeepLink_request() = runTest {
        deepLinkClient.register()

        val response = Response(
            UrlRequest(TEST_BASE_URL, HttpMethod.Post),
            HttpStatusCode.OK,
            Headers.Empty,
            bodyAsText = "{}"
        )
        everySuspend { mockNetworkClient.send(any()) } returns response
        val trackDeepLink = SdkEvent.Internal.Sdk.TrackDeepLink(
            "trackDeepLink",
            attributes = buildJsonObject {
                put("trackingId", JsonPrimitive(TRACKING_ID))
            })

        onlineEvents.emit(trackDeepLink)

        advanceUntilIdle()

        verify { mockUrlFactory.create(any()) }
        verifySuspend { mockNetworkClient.send(any()) }
    }
}