package com.sap.ec.networking.clients

import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.networking.UserAgentProviderApi
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.VISITOR_ID_COOKIE_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.XP_COOKIE_KEY
import com.sap.ec.networking.ECHeaders.CONTACT_TOKEN_HEADER
import com.sap.ec.networking.RecommendationNetworkClient
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

class RecommendationNetworkClientTest {

    private lateinit var recommendationNetworkClient: RecommendationNetworkClient
    private lateinit var mockGenericNetworkClient: NetworkClientApi
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var mockUserAgentProvider: UserAgentProviderApi

    private companion object {
        private val REQUEST = UrlRequest(url = Url("https://test.com"), method = HttpMethod.Get)
        private const val XP_VALUE = "testXpValue"
        private const val OTHER_XP_VALUE = "otherTestXpValue"
        private const val CVD_VALUE = "testCvdValue"
        private const val OTHER_CVD_VALUE = "otherTestCvdValue"
        private const val CONTACT_TOKEN = "testContactToken"
        private const val CVD_COOKIE_VALUE =
            "cdv=$CVD_VALUE; Path=/; Expires=Thu, 9 Sep 2027 18:12:36 GMT; Max-Age=31556952; Secure; SameSite=None"
        private const val OTHER_CVD_COOKIE_VALUE =
            "cdv=$OTHER_CVD_VALUE; Path=/; Expires=Thu, 9 Sep 2027 18:12:36 GMT; Max-Age=31556952; Secure; SameSite=None"
        private const val XP_COOKIE_VALUE =
            "xp=$XP_VALUE; Path=/; Expires=Thu, 9 Sep 2027 18:12:36 GMT; Max-Age=31556952; Secure; SameSite=None"
        private const val OTHER_XP_COOKIE_VALUE =
            "xp=$OTHER_XP_VALUE; Path=/; Expires=Thu, 9 Sep 2027 18:12:36 GMT; Max-Age=31556952; Secure; SameSite=None"
        private val SUCCESS_RESPONSE_WITH_COOKIES = Result.success(
            Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.build {
                    append("set-cookie", CVD_COOKIE_VALUE)
                    append("set-cookie", XP_COOKIE_VALUE)
                },
                bodyAsText = ""
            )
        )
        private val SUCCESS_RESPONSE_WITH_DIFFERENT_COOKIES = Result.success(
            Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.build {
                    append("set-cookie", OTHER_CVD_COOKIE_VALUE)
                    append("set-cookie", OTHER_XP_COOKIE_VALUE)
                },
                bodyAsText = ""
            )
        )
        private val SUCCESS_RESPONSE_WITHOUT_COOKIES = Result.success(
            Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.build {},
                bodyAsText = ""
            )
        )
        private const val USER_AGENT_HEADER = "User-Agent"
        private const val USER_AGENT_STRING = "Engagement Cloud SDK 4.0.0 IOS 26.0"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockGenericNetworkClient = mock(MockMode.autofill)
        mockStringStorage = mock(MockMode.autofill)
        mockRequestContext = mock(MockMode.autofill)
        mockUserAgentProvider = mock(MockMode.autofill)
        recommendationNetworkClient =
            RecommendationNetworkClient(
                mockGenericNetworkClient,
                mockStringStorage,
                mockRequestContext,
                mockUserAgentProvider,
                sdkLogger = mock(MockMode.autofill)
            )
        everySuspend { mockUserAgentProvider.provide() } returns USER_AGENT_STRING
    }

    @Test
    fun test_send_shouldSendRequest_withPredictHeaders_whenStringStorageIsNotEmpty_andNotStoreCookies_WhenSuccessResponseCookiesAreTheSameAsStringStorageCookies() =
        runTest {
            every { mockStringStorage.get(XP_COOKIE_KEY) } returns XP_VALUE
            every { mockStringStorage.get(VISITOR_ID_COOKIE_KEY) } returns CVD_VALUE
            every { mockRequestContext.contactToken } returns CONTACT_TOKEN
            val expectedHeaders =
                mapOf(
                    "Cookie" to "xp=$XP_VALUE;cdv=$CVD_VALUE",
                    CONTACT_TOKEN_HEADER to CONTACT_TOKEN,
                    USER_AGENT_HEADER to USER_AGENT_STRING,
                    USER_AGENT_HEADER to USER_AGENT_STRING
                )
            val requestCaptor = slot<UrlRequest>()
            everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITH_COOKIES

            recommendationNetworkClient.send(REQUEST)

            verify {
                mockStringStorage.get(XP_COOKIE_KEY)
                mockStringStorage.get(VISITOR_ID_COOKIE_KEY)
                mockRequestContext.contactToken
                requestCaptor.get().headers shouldBe expectedHeaders
            }
            verify(VerifyMode.exactly(0)) {
                mockStringStorage.put(VISITOR_ID_COOKIE_KEY, CVD_VALUE)
                mockStringStorage.put(XP_COOKIE_KEY, XP_VALUE)
            }
            verifySuspend { mockUserAgentProvider.provide() }
        }

    @Test
    fun test_send_shouldSendRequest_withPredictHeaders_whenStringStorageIsNotEmpty_andStoreCookies_WhenSuccessResponseCookiesAreDifferFromStringStorageCookies() =
        runTest {
            every { mockStringStorage.get(XP_COOKIE_KEY) } returns XP_VALUE
            every { mockStringStorage.get(VISITOR_ID_COOKIE_KEY) } returns CVD_VALUE
            every { mockRequestContext.contactToken } returns CONTACT_TOKEN
            val expectedHeaders =
                mapOf(
                    "Cookie" to "xp=$XP_VALUE;cdv=$CVD_VALUE",
                    CONTACT_TOKEN_HEADER to CONTACT_TOKEN,
                    USER_AGENT_HEADER to USER_AGENT_STRING,
                    USER_AGENT_HEADER to USER_AGENT_STRING
                )
            val requestCaptor = slot<UrlRequest>()
            everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITH_DIFFERENT_COOKIES

            recommendationNetworkClient.send(REQUEST)

            verify {
                mockStringStorage.get(XP_COOKIE_KEY)
                mockStringStorage.get(VISITOR_ID_COOKIE_KEY)
                mockRequestContext.contactToken
                requestCaptor.get().headers shouldBe expectedHeaders
                mockStringStorage.put(XP_COOKIE_KEY, OTHER_XP_VALUE)
                mockStringStorage.put(VISITOR_ID_COOKIE_KEY, OTHER_CVD_VALUE)
            }
            verifySuspend { mockUserAgentProvider.provide() }
        }

    @Test
    fun test_send_shouldSendRequest_withContactTokenHeaderAndUserAgentHeaderOnly_whenStringStorageIsEmpty_andStoreCookies_WhenSuccessResponseIncludesThem() =
        runTest {
            every { mockStringStorage.get(XP_COOKIE_KEY) } returns null
            every { mockStringStorage.get(VISITOR_ID_COOKIE_KEY) } returns null
            every { mockRequestContext.contactToken } returns CONTACT_TOKEN
            val expectedHeaders =
                mapOf(CONTACT_TOKEN_HEADER to CONTACT_TOKEN, USER_AGENT_HEADER to USER_AGENT_STRING)
            val requestCaptor = slot<UrlRequest>()
            everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITH_COOKIES

            recommendationNetworkClient.send(REQUEST)

            verify {
                mockStringStorage.get(XP_COOKIE_KEY)
                mockStringStorage.get(VISITOR_ID_COOKIE_KEY)
                mockRequestContext.contactToken
                requestCaptor.get().headers shouldBe expectedHeaders
                mockStringStorage.put(VISITOR_ID_COOKIE_KEY, CVD_VALUE)
                mockStringStorage.put(XP_COOKIE_KEY, XP_VALUE)
            }
            verifySuspend { mockUserAgentProvider.provide() }
        }

    @Test
    fun test_send_shouldNotSendAnyPredictHeader_whenStringStorageIsEmptyString_andNotStoreCookies_WhenSuccessResponseExcludesThem() =
        runTest {
            every { mockStringStorage.get(XP_COOKIE_KEY) } returns ""
            every { mockStringStorage.get(VISITOR_ID_COOKIE_KEY) } returns ""
            every { mockRequestContext.contactToken } returns CONTACT_TOKEN
            val expectedHeaders =
                mapOf(CONTACT_TOKEN_HEADER to CONTACT_TOKEN, USER_AGENT_HEADER to USER_AGENT_STRING)
            val requestCaptor = slot<UrlRequest>()
            everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITHOUT_COOKIES

            recommendationNetworkClient.send(REQUEST)

            verify {
                requestCaptor.get().headers shouldBe expectedHeaders
                mockStringStorage.get(XP_COOKIE_KEY)
                mockStringStorage.get(VISITOR_ID_COOKIE_KEY)
            }
            verify(VerifyMode.exactly(0)) {
                mockStringStorage.put(VISITOR_ID_COOKIE_KEY, CVD_VALUE)
                mockStringStorage.put(XP_COOKIE_KEY, XP_VALUE)
            }
            verifySuspend { mockUserAgentProvider.provide() }
        }

    @Test
    fun test_send_shouldReturnResultFailure_whenContactTokenIsMissing() = runTest {
        every { mockStringStorage.get(XP_COOKIE_KEY) } returns null
        every { mockStringStorage.get(VISITOR_ID_COOKIE_KEY) } returns null
        every { mockRequestContext.contactToken } returns null

        val result = recommendationNetworkClient.send(REQUEST)

        verify {
            mockRequestContext.contactToken
        }
        verifySuspend {
            result.isFailure shouldBe true
            result.exceptionOrNull()
                .shouldBeInstanceOf<SdkException.ContactTokenNotFoundException>()
        }
    }

    @Test
    fun test_send_shouldNotSendAnyPredictHeader_whenStringStorageIsNotEmpty_andContactTokenIsNotEmpty() =
        runTest {
            every { mockStringStorage.get(XP_COOKIE_KEY) } returns XP_VALUE
            every { mockStringStorage.get(VISITOR_ID_COOKIE_KEY) } returns CVD_VALUE
            every { mockRequestContext.contactToken } returns CONTACT_TOKEN
            val expectedHeaders =
                mapOf(
                    "Cookie" to "xp=$XP_VALUE;cdv=$CVD_VALUE",
                    CONTACT_TOKEN_HEADER to CONTACT_TOKEN,
                    USER_AGENT_HEADER to USER_AGENT_STRING
                )
            val requestCaptor = slot<UrlRequest>()
            everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITH_COOKIES

            recommendationNetworkClient.send(REQUEST)

            verify {
                requestCaptor.get().headers shouldBe expectedHeaders
                mockStringStorage.get(XP_COOKIE_KEY)
                mockStringStorage.get(VISITOR_ID_COOKIE_KEY)
                mockRequestContext.contactToken
            }
            verifySuspend { mockUserAgentProvider.provide() }
        }

    @Test
    fun test_send_shouldOnlyAddExistingPredictHeaders() = runTest {
        every { mockStringStorage.get(XP_COOKIE_KEY) } returns XP_VALUE
        every { mockStringStorage.get(VISITOR_ID_COOKIE_KEY) } returns ""
        every { mockRequestContext.contactToken } returns CONTACT_TOKEN
        val expectedHeaders =
            mapOf(
                "Cookie" to "xp=$XP_VALUE;",
                CONTACT_TOKEN_HEADER to CONTACT_TOKEN,
                USER_AGENT_HEADER to USER_AGENT_STRING
            )
        val requestCaptor = slot<UrlRequest>()
        everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITH_COOKIES

        recommendationNetworkClient.send(REQUEST)

        verify {
            requestCaptor.get().headers shouldBe expectedHeaders
            mockStringStorage.get(XP_COOKIE_KEY)
            mockStringStorage.get(VISITOR_ID_COOKIE_KEY)
            mockRequestContext.contactToken
        }
        verifySuspend { mockUserAgentProvider.provide() }
    }
}