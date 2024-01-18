package com.emarsys.networking

import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.networking.clients.event.model.CustomEvent
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.providers.Provider
import com.emarsys.session.SessionContext
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.UrlFactoryApi
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.config
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

class EmarsysClientTests : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockTimestampProvider: Provider<Instant>

    @Mock
    lateinit var mockUrlFactory: UrlFactoryApi

    private lateinit var networkClient: NetworkClientApi
    private lateinit var json: Json
    private lateinit var sessionContext: SessionContext

    private lateinit var emarsysClient: EmarsysClient

    private val now = Clock.System.now()

    @BeforeTest
    fun setup() = runTest {
        sessionContext = SessionContext(refreshToken = "testRefreshToken")
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
                    ByteReadChannel("""{"eventName":"test"}"""),
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
        every { mockUrlFactory.create(isAny()) } returns Url("https://testUrl.com")

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
            mapOf(
                "X-Contact-Token" to "testContactToken",
                "X-Request-Order" to now.toString()
            )
        )
        val expectedResponse = Response(
            request,
            HttpStatusCode.OK,
            headers {
                append("Content-Type", "application/json")
                append("X-Client-State", "testClientState")
            },
            """{"eventName":"test"}"""
        )

        val response: Response = emarsysClient.send(request)

        response shouldBe expectedResponse
        response.body<CustomEvent>() shouldBe CustomEvent("test")
        sessionContext.clientState shouldBe "testClientState"
    }

}