@file:OptIn(ExperimentalCoroutinesApi::class)

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
import com.emarsys.networking.EmarsysHeaders.CLIENT_ID_HEADER
import com.emarsys.networking.EmarsysHeaders.CLIENT_STATE_HEADER
import com.emarsys.networking.EmarsysHeaders.CONTACT_TOKEN_HEADER
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EmarsysClientTests {
    private companion object {
        const val ID = "testId"
        const val NAME = "testName"
        const val CLIENT_ID = "testClientId"
        const val CLIENT_STATE = "testClientState"
        const val CONTACT_TOKEN = "testContactToken"
        val testData = TestDataClass(ID, NAME)
    }

    private lateinit var mockTimestampProvider: Provider<Instant>
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockNetworkClient: NetworkClientApi
    private lateinit var json: Json
    private lateinit var sessionContext: SessionContext
    private lateinit var emarsysClient: EmarsysClient
    private val now = Clock.System.now()
    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockTimestampProvider = mock()
        mockUrlFactory = mock()
        mockNetworkClient = mock()
        sdkEventFlow = MutableSharedFlow()
        sessionContext = SessionContext(refreshToken = "testRefreshToken", deviceEventState = null)
        json = Json

        every { mockTimestampProvider.provide() } returns now
        every {
            mockUrlFactory.create(
                EmarsysUrlType.REFRESH_TOKEN,
                null
            )
        } returns Url("https://testUrl.com")

        emarsysClient = EmarsysClient(
            mockNetworkClient,
            sessionContext,
            mockTimestampProvider,
            mockUrlFactory,
            json,
            mock(MockMode.autofill),
            sdkEventFlow,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSend_should_retry_on401_and_try_to_get_refreshToken() = runTest {
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
                        append("ems-client-state", "testClientState")
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
        val networkClient = GenericNetworkClient(httpClient, sdkLogger = mock(MockMode.autofill))
        val emarsysClient = EmarsysClient(
            networkClient,
            sessionContext,
            mockTimestampProvider,
            mockUrlFactory,
            json,
            mock(MockMode.autofill),
            sdkEventFlow,
        )

        sessionContext.clientState = null
        val urlString =
            URLBuilder("https://testUrl.com").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Get,
            null,
        )
        val expectedRequest = request.copy(
            headers = mapOf(
                "ems-contact" to "testContactToken",
                "X-Contact-Token" to "testContactToken",
                "X-Request-Order" to now.toEpochMilliseconds()
            )
        )

        val expectedResponse = Response(
            expectedRequest,
            HttpStatusCode.OK,
            headers {
                append("Content-Type", "application/json")
                append("ems-client-state", "testClientState")
            },
            json.encodeToString(testData)
        )

        val response: Response = emarsysClient.send(request)

        response shouldBe expectedResponse
        response.body<TestDataClass>() shouldBe testData
        sessionContext.clientState shouldBe "testClientState"
    }

    @Test
    fun testSend_should_addEmarsysHeaders() = runTest {
        val mockHttpEngine = MockEngine.config {
            addHandler {
                respond(
                    ByteReadChannel(json.encodeToString(testData)),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json")
                )
            }
        }
        val httpClient = HttpClient(mockHttpEngine) {
            install(ContentNegotiation) {
                json
            }
            install(HttpRequestRetry)
        }
        val networkClient = GenericNetworkClient(httpClient, sdkLogger = mock(MockMode.autofill))
        val emarsysClient = EmarsysClient(
            networkClient,
            sessionContext,
            mockTimestampProvider,
            mockUrlFactory,
            json,
            mock(MockMode.autofill),
            sdkEventFlow,
        )

        sessionContext.contactToken = CONTACT_TOKEN
        sessionContext.clientState = CLIENT_STATE
        sessionContext.clientId = CLIENT_ID

        val urlString =
            URLBuilder("https://testUrl.com").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Get,
            null,
            mapOf(
                "test-header" to "testHeader"
            )
        )

        val response: Response = emarsysClient.send(request)

        response.originalRequest.headers?.get("test-header") shouldBe "testHeader"
        response.originalRequest.headers?.get(CLIENT_ID_HEADER) shouldBe CLIENT_ID
        response.originalRequest.headers?.get(CLIENT_STATE_HEADER) shouldBe CLIENT_STATE
        response.originalRequest.headers?.get(CONTACT_TOKEN_HEADER) shouldBe CONTACT_TOKEN
    }

    @Test
    fun testSend_should_emit_ReregistrationRequiredEvent_toSdkEventFlow_whenStatusCodeIs_inRange_1100_1199() =
        runTest {
            everySuspend { mockNetworkClient.send(any()) } returns Response(
                UrlRequest(
                    Url("https://testUrl.com"),
                    HttpMethod.Get,
                    null,
                ),
                HttpStatusCode(1100, "test"),
                headersOf("Content-Type", "application/json"),
                ""
            )
            CoroutineScope(Dispatchers.Main).launch {
                emarsysClient.send(UrlRequest(Url("https://testUrl.com"), HttpMethod.Get, null))
            }
            val event = sdkEventFlow.first()
            event.name shouldBe "ReregistrationRequired"
        }

    @Test
    fun testSend_should_emit_RemoteConfigUpdateRequiredEvent_toSdkEventFlow_whenStatusCodeIs_inRange_1200_1299() =
        runTest {
            everySuspend { mockNetworkClient.send(any()) } returns Response(
                UrlRequest(
                    Url("https://testUrl.com"),
                    HttpMethod.Get,
                    null,
                ),
                HttpStatusCode(1200, "test"),
                headersOf("Content-Type", "application/json"),
                ""
            )
            CoroutineScope(Dispatchers.Main).launch {
                emarsysClient.send(UrlRequest(Url("https://testUrl.com"), HttpMethod.Get, null))
            }
            val event = sdkEventFlow.first()
            event.name shouldBe "RemoteConfigUpdateRequired"
        }
}