package com.emarsys.networking

import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.core.providers.Provider
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.model.TestDataClass
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.config
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.headers
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class EmarsysClientTests {
    private companion object {
        const val id = "testId"
        const val name = "testName"
        val testData = TestDataClass(id, name)
    }

    private lateinit var mockTimestampProvider: Provider<Instant>
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var networkClient: NetworkClientApi
    private lateinit var json: Json
    private lateinit var sessionContext: SessionContext
    private lateinit var emarsysClient: EmarsysClient
    private val now = Clock.System.now()

    @BeforeTest
    fun setup() = runTest {
        mockTimestampProvider = mock()
        mockUrlFactory = mock()

        sessionContext = SessionContext(refreshToken = "testRefreshToken", deviceEventState = null)
        json = Json
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
                    ByteReadChannel("""{"contactToken":"testContactToken"}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json")
                )
            }
            addHandler {
                respond(
                    ByteReadChannel(json.encodeToString(testData)),
                    status = HttpStatusCode.OK,
                    headers = headers {
                        append("Content-Type", "application/json")
                        append("X-Client-State", "testClientState")
                    }
                )
            }
        }
        val httpClient = HttpClient(mockHttpEngine) {
            install(ContentNegotiation) {
                json
            }
            install(HttpRequestRetry)
        }
        every { mockTimestampProvider.provide() } returns now
        every { mockUrlFactory.create(EmarsysUrlType.REFRESH_TOKEN) } returns Url("https://testUrl.com")

        networkClient = GenericNetworkClient(httpClient)
        emarsysClient = EmarsysClient(
            networkClient,
            sessionContext,
            mockTimestampProvider,
            mockUrlFactory,
            json
        )
    }

    @Test
    fun testSend_should_retry_on401_and_try_to_get_refreshToken() = runTest {
        val urlString =
            URLBuilder("https://testUrl.com").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Get,
            null,
        )
        val expectedRequest = request.copy(
            headers = mapOf(
                "X-Contact-Token" to "testContactToken",
                "X-Request-Order" to now.toEpochMilliseconds()
            )
        )

        val expectedResponse = Response(
            expectedRequest,
            HttpStatusCode.OK,
            headers {
                append("Content-Type", "application/json")
                append("X-Client-State", "testClientState")
            },
            json.encodeToString(testData)
        )

        val response: Response = emarsysClient.send(request)

        response shouldBe expectedResponse
        response.body<TestDataClass>() shouldBe testData
        sessionContext.clientState shouldBe "testClientState"
    }

}