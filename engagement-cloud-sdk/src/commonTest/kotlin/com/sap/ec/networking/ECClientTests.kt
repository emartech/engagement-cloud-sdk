package com.sap.ec.networking

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.GenericNetworkClient
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.event.SdkEvent
import com.sap.ec.model.TestDataClass
import com.sap.ec.networking.ECHeaders.CLIENT_ID_HEADER
import com.sap.ec.networking.ECHeaders.CLIENT_STATE_HEADER
import com.sap.ec.networking.ECHeaders.CONTACT_TOKEN_HEADER
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.isPresent
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ECClientTests {
    private companion object Companion {
        const val ID = "testId"
        const val NAME = "testName"
        const val CLIENT_ID = "testClientId"
        const val CLIENT_STATE = "testClientState"
        const val CONTACT_TOKEN = "testContactToken"
        val testData = TestDataClass(ID, NAME)
    }

    private lateinit var mockNetworkClient: NetworkClientApi
    private lateinit var mockContactTokenRefresher: ContactTokenRefresherApi
    private lateinit var json: Json
    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var eCClient: ECClient
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkLogger: Logger

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockNetworkClient = mock()
        mockContactTokenRefresher = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockRequestContext = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)

        every { mockRequestContext.contactToken } returns CONTACT_TOKEN
        every { mockRequestContext.clientState } returns CLIENT_STATE
        every { mockRequestContext.clientId } returns CLIENT_ID

        json = JsonUtil.json

        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)

        everySuspend { mockContactTokenRefresher.executeWithTokenRefresh(any(), any()) } calls { args ->
            @Suppress("UNCHECKED_CAST")
            val callback = args.arg<suspend () -> Result<Response>>(1)
            callback()
        }

        eCClient = ECClient(
            mockNetworkClient,
            mockRequestContext,
            mockContactTokenRefresher,
            mockSdkLogger,
            mockSdkEventDistributor,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSend_should_addEcHeaders() = runTest {
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
            install(HttpRequestRetry)
        }
        val networkClient = GenericNetworkClient(httpClient, mockSdkLogger)
        val eCClient = ECClient(
            networkClient,
            mockRequestContext,
            mockContactTokenRefresher,
            mock(MockMode.autofill),
            mockSdkEventDistributor,
        )

        val urlString = URLBuilder("https://testUrl.com").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Get,
            null,
            mapOf("test-header" to "testHeader")
        )

        val response: Response = eCClient.send(request).getOrNull()!!

        response.originalRequest.headers?.get("test-header") shouldBe "testHeader"
        response.originalRequest.headers?.get(CLIENT_ID_HEADER) shouldBe CLIENT_ID
        response.originalRequest.headers?.get(CLIENT_STATE_HEADER) shouldBe CLIENT_STATE
        response.originalRequest.headers?.get(CONTACT_TOKEN_HEADER) shouldBe CONTACT_TOKEN
    }

    @Test
    fun testSend_should_emit_ReregistrationRequiredEvent_toSdkEventFlow_whenStatusCodeIs_inRange_400_504_andErrorCodeIsInRange_1100_1199() =
        runTest {
            val eventSlot = slot<SdkEvent.Internal.Sdk.ReregistrationRequired>()

            everySuspend { mockNetworkClient.send(any()) } returns Result.success(
                Response(
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
            )

            everySuspend {
                mockSdkEventDistributor.registerEvent(
                    sdkEvent = capture(eventSlot)
                )
            } returns mock(MockMode.autofill)

            eCClient.send(UrlRequest(Url("https://testUrl.com"), HttpMethod.Get, null))

            advanceUntilIdle()

            eventSlot.isPresent shouldBe true
        }

    @Test
    fun testSend_should_emit_RemoteConfigUpdateRequiredEvent_toSdkEventFlow_whenStatusCodeIs_inRange_400_504_andErrorCodeIsInRange_1200_1299() =
        runTest {
            val eventSlot = slot<SdkEvent.Internal.Sdk.RemoteConfigUpdateRequired>()

            everySuspend { mockNetworkClient.send(any()) } returns Result.success(
                Response(
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
                        })
                    }.toString()
                )
            )
            everySuspend {
                mockSdkEventDistributor.registerEvent(
                    sdkEvent = capture(eventSlot)
                )
            } returns mock(MockMode.autofill)

            eCClient.send(UrlRequest(Url("https://testUrl.com"), HttpMethod.Get, null))

            advanceUntilIdle()

            eventSlot.isPresent shouldBe true
        }

    @Test
    fun testSend_should_not_crash_and_log_when_errorCode_cant_be_parsed_toInt() = runTest {
        val invalidErrorCode = "STRING_INSTEAD_OF_NUMBER"
        everySuspend { mockNetworkClient.send(any()) } returns Result.success(
            Response(
                UrlRequest(
                    Url("https://testUrl.com"),
                    HttpMethod.Get,
                    null,
                ),
                HttpStatusCode(401, "test"),
                headersOf("Content-Type", "application/json"),
                buildJsonObject {
                    put("error", buildJsonObject {
                        put("code", invalidErrorCode)
                        put("message", "Anything")
                        put("target", "Anything")
                        putJsonArray("details") {
                            add(buildJsonObject {
                                put("code", "1201")
                                put("message", "TestMessage")
                            })
                        }
                    })
                }.toString()
            )
        )

        eCClient.send(UrlRequest(Url("https://testUrl.com"), HttpMethod.Get, null))

        verifySuspend {
            mockSdkLogger.debug(
                "Response error code can't be parsed to Int. Error code value: $invalidErrorCode",
                true
            )
        }
        verifySuspend(VerifyMode.exactly(0)) {
            mockSdkEventDistributor.registerEvent(any())
        }
    }

    @Test
    fun testSend_should_delegateToContactTokenRefresher() = runTest {
        everySuspend { mockNetworkClient.send(any()) } returns Result.success(
            Response(
                UrlRequest(Url("https://testUrl.com"), HttpMethod.Get, null),
                HttpStatusCode.OK,
                headersOf("Content-Type", "application/json"),
                "{}"
            )
        )

        eCClient.send(UrlRequest(Url("https://testUrl.com"), HttpMethod.Get, null))

        verifySuspend { mockContactTokenRefresher.executeWithTokenRefresh(any(), any()) }
    }
}
