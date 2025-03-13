package com.emarsys.networking.clients.contact

import com.emarsys.EmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactClientTests {
    private companion object {
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val CONTACT_FIELD_ID = 2575
        const val MERCHANT_ID = "testMerchantId"
    }

    private val json: Json = JsonUtil.json
    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockContactTokenHandler: ContactTokenHandlerApi
    private lateinit var contactClient: ContactClientApi

    @BeforeTest
    fun setUp() {
        mockEmarsysClient = mock()
        mockUrlFactory = mock()
        mockSdkContext = mock()
        mockContactTokenHandler = mock()
        contactClient = ContactClient(
            mockEmarsysClient,
            mockUrlFactory,
            mockSdkContext,
            mockContactTokenHandler,
            json,
            sdkLogger = mock(MockMode.autofill)
        )
    }

    @Test
    fun testLinkContact_should_send_request_to_client_service_withContactFieldValue() = runTest {
        every { mockSdkContext.config } returns EmarsysConfig(
            merchantId = null
        )
        val testUrl = Url("https://www.testUrl.com/testAppCode/client")
        val testBody =
            """{"contactFieldId":2575,"contactFieldValue":"testContactFieldValue"}"""
        val expectedUrlRequest = UrlRequest(
            testUrl,
            HttpMethod.Post,
            testBody,
            mapOf()
        )
        val expectedResponse = Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
        )
        every { mockUrlFactory.create(EmarsysUrlType.LINK_CONTACT) } returns testUrl
        everySuspend { mockEmarsysClient.send(expectedUrlRequest) } returns expectedResponse
        everySuspend { mockContactTokenHandler.handleContactTokens(expectedResponse) } returns Unit

        contactClient.linkContact(CONTACT_FIELD_ID, contactFieldValue = CONTACT_FIELD_VALUE)


        verifySuspend {
            mockEmarsysClient.send(expectedUrlRequest)
            mockContactTokenHandler.handleContactTokens(expectedResponse)
        }
    }

    @Test
    fun testLinkContact_should_send_request_to_client_service_with_openId() = runTest {
        every { mockSdkContext.config } returns EmarsysConfig(
            merchantId = null
        )
        val testUrl = Url("https://www.testUrl.com/testAppCode/client")
        val testBody =
            """{"contactFieldId":2575,"openIdToken":"$OPEN_ID_TOKEN"}"""
        val expectedUrlRequest = UrlRequest(
            testUrl,
            HttpMethod.Post,
            testBody,
            mapOf()
        )
        val expectedResponse = Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
        )
        every { mockUrlFactory.create(EmarsysUrlType.LINK_CONTACT) } returns testUrl
        everySuspend { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
        )
        everySuspend { mockContactTokenHandler.handleContactTokens(expectedResponse) } returns Unit


        val response = contactClient.linkContact(CONTACT_FIELD_ID, openIdToken = OPEN_ID_TOKEN)

        verifySuspend {
            mockEmarsysClient.send(expectedUrlRequest)
        }

        response shouldBe expectedResponse
    }

    @Test
    fun testLinkContact_should_send_request_to_client_service_with_openId_and_merchantIdAvailable() =
        runTest {
            every { mockSdkContext.config } returns EmarsysConfig(
                merchantId = MERCHANT_ID
            )

            val testUrl = Url("https://www.testUrl.com/testAppCode/client")
            val testBody =
                """{"contactFieldId":2575,"openIdToken":"$OPEN_ID_TOKEN"}"""
            val expectedUrlRequest = UrlRequest(
                testUrl,
                HttpMethod.Post,
                testBody,
                mapOf("ems-merchant-id" to MERCHANT_ID)
            )

            val expectedResponse = Response(
                expectedUrlRequest,
                HttpStatusCode.OK,
                headers {
                    append("ems-merchant-id", MERCHANT_ID)
                },
                """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
            )
            every { mockUrlFactory.create(EmarsysUrlType.LINK_CONTACT) } returns testUrl
            everySuspend { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
                expectedUrlRequest,
                HttpStatusCode.OK,
                headers {
                    append("ems-merchant-id", MERCHANT_ID)
                },
                """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
            )
            everySuspend { mockContactTokenHandler.handleContactTokens(expectedResponse) } returns Unit

            val response = contactClient.linkContact(CONTACT_FIELD_ID, openIdToken = OPEN_ID_TOKEN)

            verifySuspend {
                mockEmarsysClient.send(expectedUrlRequest)
            }

            response shouldBe expectedResponse
        }

    @Test
    fun testUnLinkContact_should_send_request_to_client_service_andHandleContactTokens() = runTest {
        every { mockSdkContext.config } returns EmarsysConfig(
            merchantId = null
        )
        val testUrl = Url("https://www.testUrl.com/testAppCode/client?anonymous=true")

        val expectedUrlRequest = UrlRequest(
            testUrl,
            HttpMethod.Delete,
            null,
            mapOf()
        )
        every { mockUrlFactory.create(EmarsysUrlType.UNLINK_CONTACT) } returns testUrl
        everySuspend { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
        )

        val expectedResponse = Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
        )
        everySuspend { mockContactTokenHandler.handleContactTokens(expectedResponse) } returns Unit

        val response = contactClient.unlinkContact()

        verifySuspend {
            mockEmarsysClient.send(expectedUrlRequest)
            mockContactTokenHandler.handleContactTokens(expectedResponse)
        }

        response shouldBe expectedResponse
    }

    @Test
    fun testUnLinkContact_should_send_request_to_client_service_with_merchantIdAvailable() =
        runTest {
            every { mockSdkContext.config } returns EmarsysConfig(
                merchantId = MERCHANT_ID
            )

            val testUrl = Url("https://www.testUrl.com/testAppCode/client")

            val expectedUrlRequest = UrlRequest(
                testUrl,
                HttpMethod.Delete,
                null,
                mapOf("ems-merchant-id" to MERCHANT_ID)
            )
            every { mockUrlFactory.create(EmarsysUrlType.UNLINK_CONTACT) } returns testUrl
            everySuspend { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
                expectedUrlRequest,
                HttpStatusCode.OK,
                headers {
                    append("ems-merchant-id", MERCHANT_ID)
                },
                """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
            )

            val expectedResponse = Response(
                expectedUrlRequest,
                HttpStatusCode.OK,
                headers {
                    append("ems-merchant-id", MERCHANT_ID)
                },
                """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
            )
            everySuspend { mockContactTokenHandler.handleContactTokens(expectedResponse) } returns Unit

            val response = contactClient.unlinkContact()

            verifySuspend {
                mockEmarsysClient.send(expectedUrlRequest)
                mockContactTokenHandler.handleContactTokens(expectedResponse)
            }

            response shouldBe expectedResponse
        }
}