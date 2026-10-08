package com.sap.ec.networking

import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.GenericNetworkClient
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.model.TestDataClass
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsBy
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
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class ContactTokenRefresherTests {

    private companion object {
        const val REFRESH_TOKEN = "testRefreshToken"
        const val NEW_CONTACT_TOKEN = "newContactToken"
        const val CLIENT_ID = "testClientId"
        const val CLIENT_STATE = "testClientState"
        val testData = TestDataClass("testId", "testName")
        val BASE_URL: Url = URLBuilder("https://testUrl.com").build()
    }

    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var json: Json

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockUrlFactory = mock()
        mockRequestContext = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
        json = JsonUtil.json

        every { mockRequestContext.refreshToken } returns REFRESH_TOKEN
        every { mockRequestContext.clientId } returns CLIENT_ID
        every { mockRequestContext.clientState } returns CLIENT_STATE

        everySuspend { mockUrlFactory.create(ECUrlType.RefreshToken) } returns BASE_URL
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildRefresher(networkClient: NetworkClientApi) = ContactTokenRefresher(
        networkClient = networkClient,
        requestContext = mockRequestContext,
        urlFactory = mockUrlFactory,
        json = json,
        sdkLogger = mockSdkLogger
    )

    private fun unauthorizedResponse() = ByteReadChannel(
        buildJsonObject {
            put("error", buildJsonObject {
                put("code", "1000")
                put("message", "the contact-token needs to be refreshed")
                put("target", "/v4/apps/EMS-1234/client")
            })
        }.toString()
    )

    @Test
    fun testExecuteWithTokenRefresh_should_retry_on401_andRefreshToken_andRetryOriginalRequest() = runTest {
        val mockHttpEngine = MockEngine.config {
            addHandler {
                respond(
                    unauthorizedResponse(),
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
                    ByteReadChannel(json.encodeToString(testData)),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json")
                )
            }
        }
        val networkClient = GenericNetworkClient(HttpClient(mockHttpEngine) { install(HttpRequestRetry) }, mockSdkLogger)
        val refresher = buildRefresher(networkClient)

        val request = UrlRequest(BASE_URL, HttpMethod.Get, null)
        var callCount = 0
        val result = refresher.executeWithTokenRefresh {
            callCount++
            networkClient.send(request)
        }

        result.isSuccess shouldBe true
        callCount shouldBe 2
        verifySuspend { mockRequestContext.contactToken = NEW_CONTACT_TOKEN }
    }

    @Test
    fun testExecuteWithTokenRefresh_should_not_retry_when_refreshToken_is_null() = runTest {
        every { mockRequestContext.refreshToken } returns null

        val mockHttpEngine = MockEngine.config {
            addHandler {
                respond(
                    unauthorizedResponse(),
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf("Content-Type", "application/json")
                )
            }
        }
        val networkClient = GenericNetworkClient(HttpClient(mockHttpEngine) { install(HttpRequestRetry) }, mockSdkLogger)
        val refresher = buildRefresher(networkClient)

        val request = UrlRequest(BASE_URL, HttpMethod.Get, null)
        var callCount = 0
        val result = refresher.executeWithTokenRefresh {
            callCount++
            networkClient.send(request)
        }

        result.isFailure shouldBe true
        callCount shouldBe 1
    }

    @Test
    fun testExecuteWithTokenRefresh_should_return_original_failure_when_refreshRequest_fails() = runTest {
        val mockNetworkClient: NetworkClientApi = mock()
        val unauthorizedBody = buildJsonObject {
            put("error", buildJsonObject {
                put("code", "1000")
                put("message", "the contact-token needs to be refreshed")
                put("target", "/v4/apps/EMS-1234/client")
            })
        }.toString()
        val unauthorizedResponse = Response(
            UrlRequest(BASE_URL, HttpMethod.Get, null),
            HttpStatusCode.Unauthorized,
            headersOf("Content-Type", "application/json"),
            unauthorizedBody
        )
        val originalFailure = Result.failure<Response>(SdkException.FailedRequestException(unauthorizedResponse))
        var callCount = 0
        everySuspend { mockNetworkClient.send(any()) }.returnsBy {
            callCount++
            if (callCount == 1) originalFailure
            else Result.failure(RuntimeException("refresh endpoint down"))
        }

        val refresher = buildRefresher(mockNetworkClient)
        val result = refresher.executeWithTokenRefresh { mockNetworkClient.send(UrlRequest(BASE_URL, HttpMethod.Get, null)) }

        result shouldBe originalFailure
    }

    @Test
    fun testExecuteWithTokenRefresh_should_not_retry_beyond_maxRetryCount() = runTest {
        val mockHttpEngine = MockEngine.config {
            repeat(10) {
                addHandler {
                    respond(
                        unauthorizedResponse(),
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
            }
        }
        val networkClient = GenericNetworkClient(HttpClient(mockHttpEngine) { install(HttpRequestRetry) }, mockSdkLogger)
        val refresher = buildRefresher(networkClient)

        val request = UrlRequest(BASE_URL, HttpMethod.Get, null)
        var callCount = 0
        val result = refresher.executeWithTokenRefresh {
            callCount++
            networkClient.send(request)
        }

        result.isFailure shouldBe true
        callCount shouldBe 4
    }

    @Test
    fun testExecuteWithTokenRefresh_should_log_retryCount_and_status_on_retry() = runTest {
        val mockHttpEngine = MockEngine.config {
            addHandler {
                respond(
                    unauthorizedResponse(),
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
                    ByteReadChannel(json.encodeToString(testData)),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json")
                )
            }
        }
        val networkClient = GenericNetworkClient(HttpClient(mockHttpEngine) { install(HttpRequestRetry) }, mockSdkLogger)
        val refresher = buildRefresher(networkClient)

        refresher.executeWithTokenRefresh { networkClient.send(UrlRequest(BASE_URL, HttpMethod.Get, null)) }

        verifySuspend {
            mockSdkLogger.debug(
                "refreshing contact token",
                buildJsonObject {
                    put("retryCount", 0L)
                    put("status", HttpStatusCode.Unauthorized.value)
                }
            )
        }
    }
}
