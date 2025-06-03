@file:OptIn(ExperimentalCoroutinesApi::class)

package com.emarsys.networking

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.model.TestDataClass
import com.emarsys.networking.EmarsysHeaders.CLIENT_ID_HEADER
import com.emarsys.networking.EmarsysHeaders.CLIENT_STATE_HEADER
import com.emarsys.networking.EmarsysHeaders.CONTACT_TOKEN_HEADER
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.*
import dev.mokkery.answering.returns
import dev.mokkery.matcher.any
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.serialization.json.*
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

    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockNetworkClient: NetworkClientApi
    private lateinit var json: Json
    private lateinit var sessionContext: SessionContext
    private lateinit var emarsysClient: EmarsysClient
    private val now = Clock.System.now()
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockTimestampProvider = mock()
        mockUrlFactory = mock()
        mockNetworkClient = mock()
        mockSdkEventDistributor = mock(MockMode.autofill)
        sessionContext = SessionContext(refreshToken = null, deviceEventState = null)
        json = Json

        every { mockTimestampProvider.provide() } returns now
        every {
            mockUrlFactory.create(EmarsysUrlType.REFRESH_TOKEN)
        } returns Url("https://testUrl.com")

        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)

        emarsysClient = EmarsysClient(
            mockNetworkClient,
            sessionContext,
            mockTimestampProvider,
            mockUrlFactory,
            json,
            mock(MockMode.autofill),
            mockSdkEventDistributor,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSend_should_retry_on401_and_try_to_get_refreshToken() = runTest {
        sessionContext.refreshToken = "testRefreshToken"

        val mockHttpEngine = MockEngine.config {
            addHandler {
                respond(
                    ByteReadChannel(
                        buildJsonObject {
                            put("error", buildJsonObject {
                                put("code", "1000")
                                put("message", "the contact-token needs to be refreshed")
                                put("target", "/v4/apps/EMS-1234/client")
                                put("details", buildJsonArray { })
                            })
                        }.toString()
                    ),
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
            mockSdkEventDistributor,
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
            mockSdkEventDistributor,
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
    fun testSend_should_emit_ReregistrationRequiredEvent_toSdkEventFlow_whenStatusCodeIs_inRange_400_504_andErrorCodeIsInRange_1100_1199() =
        runTest {

            everySuspend { mockNetworkClient.send(any()) } returns Response(
                UrlRequest(
                    Url("https://testUrl.com"),
                    HttpMethod.Get,
                    null,
                ),
                HttpStatusCode(400, "test"),
                headersOf("Content-Type", "application/json"),
                buildJsonObject {
                    put("error", buildJsonObject {
                        put("code", "1112")
                        put("message", "The contact-token could be not verified")
                        put("target", "/v4/apps/EMS-1234/client")
                        putJsonArray("details") {
                            add(buildJsonObject {
                                put("code", "1100")
                                put(
                                    "message",
                                    "refresh token could not be decrypted, complete re-registration required"
                                )
                            })
                        }
                    })
                }.toString()
            )

            emarsysClient.send(UrlRequest(Url("https://testUrl.com"), HttpMethod.Get, null))

            verifySuspend {
                mockSdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.ReregistrationRequired(
                        id = any()
                    )
                )
            }
        }

    @Test
    fun testSend_should_emit_RemoteConfigUpdateRequiredEvent_toSdkEventFlow_whenStatusCodeIs_inRange_400_504_andErrorCodeIsInRange_1200_1299() =
        runTest {
            everySuspend { mockNetworkClient.send(any()) } returns Response(
                UrlRequest(
                    Url("https://testUrl.com"),
                    HttpMethod.Get,
                    null,
                ),
                HttpStatusCode(401, "test"),
                headersOf("Content-Type", "application/json"),
                buildJsonObject {
                    put("error", buildJsonObject {
                        put("code", "1200")
                        put("message", "The contact-token could be not verified")
                        put("target", "/v4/apps/EMS-1234/client")
                        putJsonArray("details") {
                            add(buildJsonObject {
                                put("code", "1201")
                                put(
                                    "message",
                                    "refresh token could not be decrypted, complete re-registration required"
                                )
                            })
                        }
                    })}.toString()
            )

            emarsysClient.send(UrlRequest(Url("https://testUrl.com"), HttpMethod.Get, null))

            verifySuspend {
                mockSdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.RemoteConfigUpdateRequired(
                        id = any()
                    )
                )
            }
        }
}