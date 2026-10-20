package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.networking.UserAgentProviderApi
import com.sap.ec.core.networking.clients.GenericNetworkClient
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.networking.ContactTokenRefresher
import com.sap.ec.networking.RecommendationNetworkClient
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.config
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class RecommendationNetworkClientSmokeTest {

    private companion object {
        const val CONTACT_TOKEN = "testContactToken"
        const val NEW_CONTACT_TOKEN = "newContactToken"
        const val REFRESH_TOKEN = "testRefreshToken"
        const val CLIENT_ID = "testClientId"
        const val CLIENT_STATE = "testClientState"
        const val USER_AGENT_STRING = "Engagement Cloud SDK 4.0.0 IOS 26.0"
        val BASE_URL = URLBuilder("https://testUrl.com").build()
        val REFRESH_URL = URLBuilder("https://testUrl.com/refresh").build()
    }

    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockUserAgentProvider: UserAgentProviderApi
    private lateinit var mockUrlFactory: UrlFactoryApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockRequestContext = mock(MockMode.autofill)
        mockStringStorage = mock(MockMode.autofill)
        mockUserAgentProvider = mock(MockMode.autofill)
        mockUrlFactory = mock()

        every { mockRequestContext.refreshToken } returns REFRESH_TOKEN
        every { mockRequestContext.clientId } returns CLIENT_ID
        every { mockRequestContext.clientState } returns CLIENT_STATE
        every { mockStringStorage.get(any()) } returns null
        everySuspend { mockUserAgentProvider.provide() } returns USER_AGENT_STRING
        everySuspend { mockUrlFactory.create(ECUrlType.RefreshToken) } returns REFRESH_URL
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_send_shouldRefreshContactToken_andRetryRequest_when401IsReturned() = runTest {
        every { mockRequestContext.contactToken } returns CONTACT_TOKEN

        val mockHttpEngine = MockEngine.config {
            addHandler {
                respond(
                    ByteReadChannel(""),
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf("Content-Type", "application/json")
                )
            }
            addHandler {
                respond(
                    ByteReadChannel("""{"contactToken":"$NEW_CONTACT_TOKEN"}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json")
                )
            }
            addHandler {
                respond(
                    ByteReadChannel(""),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json")
                )
            }
        }

        val genericNetworkClient = GenericNetworkClient(
            HttpClient(mockHttpEngine) { install(HttpRequestRetry) },
            mock(MockMode.autofill)
        )
        val contactTokenRefresher = ContactTokenRefresher(
            networkClient = genericNetworkClient,
            requestContext = mockRequestContext,
            urlFactory = mockUrlFactory,
            json = JsonUtil.json,
            sdkLogger = mock(MockMode.autofill)
        )
        val recommendationNetworkClient = RecommendationNetworkClient(
            genericNetworkClient,
            mockStringStorage,
            mockRequestContext,
            mockUserAgentProvider,
            contactTokenRefresher,
            sdkLogger = mock(MockMode.autofill)
        )

        val result = recommendationNetworkClient.send(UrlRequest(BASE_URL, HttpMethod.Get, null))

        result.isSuccess shouldBe true
        verifySuspend { mockRequestContext.contactToken = NEW_CONTACT_TOKEN }
    }

    @Test
    fun test_send_shouldRetryRequest_when500IsReturned_forRecommendationTrackEvent() = runTest {
        every { mockRequestContext.contactToken } returns CONTACT_TOKEN

        val mockHttpEngine = MockEngine.config {
            addHandler {
                respond(
                    ByteReadChannel(""),
                    status = HttpStatusCode.InternalServerError,
                    headers = headersOf("Content-Type", "application/json")
                )
            }
            addHandler {
                respond(
                    ByteReadChannel(""),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json")
                )
            }
        }

        val genericNetworkClient = GenericNetworkClient(
            HttpClient(mockHttpEngine) { install(HttpRequestRetry) },
            mock(MockMode.autofill)
        )
        val contactTokenRefresher = ContactTokenRefresher(
            networkClient = genericNetworkClient,
            requestContext = mockRequestContext,
            urlFactory = mockUrlFactory,
            json = JsonUtil.json,
            sdkLogger = mock(MockMode.autofill)
        )
        val recommendationNetworkClient = RecommendationNetworkClient(
            genericNetworkClient,
            mockStringStorage,
            mockRequestContext,
            mockUserAgentProvider,
            contactTokenRefresher,
            sdkLogger = mock(MockMode.autofill)
        )

        val trackEventRequest = UrlRequest(BASE_URL, HttpMethod.Get, null, shouldRetryOnFail = true)
        val result = recommendationNetworkClient.send(trackEventRequest)

        result.isSuccess shouldBe true
    }

    @Test
    fun test_send_shouldNotRetryRequest_when500IsReturned_forRecommendationRequest() = runTest {
        every { mockRequestContext.contactToken } returns CONTACT_TOKEN

        val mockHttpEngine = MockEngine {
            respond(
                ByteReadChannel(""),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf("Content-Type", "application/json")
            )
        }

        val genericNetworkClient = GenericNetworkClient(
            HttpClient(mockHttpEngine) { install(HttpRequestRetry) },
            mock(MockMode.autofill)
        )
        val contactTokenRefresher = ContactTokenRefresher(
            networkClient = genericNetworkClient,
            requestContext = mockRequestContext,
            urlFactory = mockUrlFactory,
            json = JsonUtil.json,
            sdkLogger = mock(MockMode.autofill)
        )
        val recommendationNetworkClient = RecommendationNetworkClient(
            genericNetworkClient,
            mockStringStorage,
            mockRequestContext,
            mockUserAgentProvider,
            contactTokenRefresher,
            sdkLogger = mock(MockMode.autofill)
        )

        val recommendationEventRequest = UrlRequest(BASE_URL, HttpMethod.Get, null, shouldRetryOnFail = false)
        val result = recommendationNetworkClient.send(recommendationEventRequest)

        result.isFailure shouldBe true
        mockHttpEngine.requestHistory.size shouldBe 1
    }
}