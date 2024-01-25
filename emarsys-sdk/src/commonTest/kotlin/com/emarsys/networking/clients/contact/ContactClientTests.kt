package com.emarsys.networking.clients.contact

import com.emarsys.EmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.UrlFactoryApi
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.Test

class ContactClientTests : TestsWithMocks() {
    private companion object {
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val CONTACT_FIELD_ID = 2575
        const val MERCHANT_ID = "testMerchantId"
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockEmarsysClient: NetworkClientApi

    @Mock
    lateinit var mockUrlFactory: UrlFactoryApi

    @Mock
    lateinit var mockSdkContext: SdkContextApi

    @Mock
    lateinit var mockContactTokenHandler: ContactTokenHandlerApi

    private val json: Json = Json
    private val contactClient: ContactClientApi by withMocks {
        ContactClient(mockEmarsysClient, mockUrlFactory, mockSdkContext, mockContactTokenHandler, json)
    }

    @AfterTest
    fun teardown() {
        mocker.reset()
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
        everySuspending { mockEmarsysClient.send(expectedUrlRequest) } returns expectedResponse
        every { mockContactTokenHandler.handleContactTokens(expectedResponse) } returns Unit

        contactClient.linkContact(CONTACT_FIELD_ID, contactFieldValue = CONTACT_FIELD_VALUE)


        verifyWithSuspend(exhaustive = false) {
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
        everySuspending { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
        )
        every { mockContactTokenHandler.handleContactTokens(expectedResponse) } returns Unit


        val response = contactClient.linkContact(CONTACT_FIELD_ID, openIdToken = OPEN_ID_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            mockEmarsysClient.send(expectedUrlRequest)
        }

        response shouldBe expectedResponse
    }

    @Test
    fun testLinkContact_should_send_request_to_client_service_with_openId_and_merchantIdAvailable() = runTest {
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
            mapOf("X-Merchant-Id" to MERCHANT_ID)
        )

        val expectedResponse = Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            headers {
                append("X-Merchant-Id", MERCHANT_ID)
            },
            """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
        )
        every { mockUrlFactory.create(EmarsysUrlType.LINK_CONTACT) } returns testUrl
        everySuspending { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            headers {
                append("X-Merchant-Id", MERCHANT_ID)
            },
            """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
        )
        every { mockContactTokenHandler.handleContactTokens(expectedResponse) } returns Unit

        val response = contactClient.linkContact(CONTACT_FIELD_ID, openIdToken = OPEN_ID_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            mockEmarsysClient.send(expectedUrlRequest)
        }

        response shouldBe expectedResponse
    }

    @Test
    fun testUnLinkContact_should_send_request_to_client_service() = runTest {
        every { mockSdkContext.config } returns EmarsysConfig(
            merchantId = null
        )
        val testUrl = Url("https://www.testUrl.com/testAppCode/client?anonymous=true")

        val expectedUrlRequest = UrlRequest(
            testUrl,
            HttpMethod.Post,
            null,
            mapOf()
        )
        every { mockUrlFactory.create(EmarsysUrlType.UNLINK_CONTACT) } returns testUrl
        everySuspending { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
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

        val response = contactClient.unlinkContact()

        verifyWithSuspend(exhaustive = false) {
            mockEmarsysClient.send(expectedUrlRequest)
        }

        response shouldBe expectedResponse
    }

    @Test
    fun testUnLinkContact_should_send_request_to_client_service_with_merchantIdAvailable() = runTest {
        every { mockSdkContext.config } returns EmarsysConfig(
            merchantId = MERCHANT_ID
        )

        val testUrl = Url("https://www.testUrl.com/testAppCode/client")

        val expectedUrlRequest = UrlRequest(
            testUrl,
            HttpMethod.Post,
            null,
            mapOf("X-Merchant-Id" to MERCHANT_ID)
        )
        every { mockUrlFactory.create(EmarsysUrlType.UNLINK_CONTACT) } returns testUrl
        everySuspending { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            headers {
                append("X-Merchant-Id", MERCHANT_ID)
            },
            """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
        )

        val expectedResponse = Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            headers {
                append("X-Merchant-Id", MERCHANT_ID)
            },
            """{"refreshToken":"testRefreshToken", "contactToken":"testContactToken"}"""
        )

        val response = contactClient.unlinkContact()

        verifyWithSuspend(exhaustive = false) {
            mockEmarsysClient.send(expectedUrlRequest)
        }

        response shouldBe expectedResponse
    }
}