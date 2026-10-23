package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.networking.UserAgentProviderApi
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.networking.ContactTokenRefresherApi
import com.sap.ec.networking.ECHeaders.CONTACT_TOKEN_HEADER
import com.sap.ec.networking.RecommendationNetworkClient
import com.sap.ec.recommendation.RecommendationConstants.SCARAB_FORCE_COHORT_HEADER
import com.sap.ec.recommendation.RecommendationConstants.SCARAB_SESSION_HEADER
import com.sap.ec.recommendation.RecommendationConstants.SCARAB_USER_PROFILE_HEADER
import com.sap.ec.recommendation.RecommendationConstants.SCARAB_VISITOR_HEADER
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
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
    private lateinit var mockContactTokenRefresher: ContactTokenRefresherApi

    private companion object {
        private val REQUEST = UrlRequest(url = Url("https://test.com"), method = HttpMethod.Get)
        private const val CONTACT_TOKEN = "testContactToken"
        private const val VISITOR_VALUE = "testVisitor"
        private const val OTHER_VISITOR_VALUE = "otherTestVisitor"
        private const val SESSION_VALUE = "testSession"
        private const val OTHER_SESSION_VALUE = "otherTestSession"
        private const val USER_PROFILE_VALUE = "testUserProfile"
        private const val OTHER_USER_PROFILE_VALUE = "otherTestUserProfile"
        private const val FORCE_COHORT_VALUE = "testForceCohort"
        private const val OTHER_FORCE_COHORT_VALUE = "otherTestForceCohort"
        private const val USER_AGENT_HEADER = "User-Agent"
        private const val USER_AGENT_STRING = "Engagement Cloud SDK 4.0.0 IOS 26.0"

        private val SUCCESS_RESPONSE_WITH_SCARAB_HEADERS = Result.success(
            Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.build {
                    append(SCARAB_VISITOR_HEADER, VISITOR_VALUE)
                    append(SCARAB_SESSION_HEADER, SESSION_VALUE)
                    append(SCARAB_USER_PROFILE_HEADER, USER_PROFILE_VALUE)
                    append(SCARAB_FORCE_COHORT_HEADER, FORCE_COHORT_VALUE)
                },
                bodyAsText = ""
            )
        )
        private val SUCCESS_RESPONSE_WITH_DIFFERENT_SCARAB_HEADERS = Result.success(
            Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.build {
                    append(SCARAB_VISITOR_HEADER, OTHER_VISITOR_VALUE)
                    append(SCARAB_SESSION_HEADER, OTHER_SESSION_VALUE)
                    append(SCARAB_USER_PROFILE_HEADER, OTHER_USER_PROFILE_VALUE)
                    append(SCARAB_FORCE_COHORT_HEADER, OTHER_FORCE_COHORT_VALUE)
                },
                bodyAsText = ""
            )
        )
        private val SUCCESS_RESPONSE_WITHOUT_SCARAB_HEADERS = Result.success(
            Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.build {},
                bodyAsText = ""
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockGenericNetworkClient = mock(MockMode.autofill)
        mockStringStorage = mock(MockMode.autofill)
        mockRequestContext = mock(MockMode.autofill)
        mockUserAgentProvider = mock(MockMode.autofill)
        mockContactTokenRefresher = mock(MockMode.autofill)

        everySuspend {
            mockContactTokenRefresher.executeWithTokenRefresh(
                any(),
                any()
            )
        } calls { args ->
            @Suppress("UNCHECKED_CAST")
            val callback = args.arg<suspend () -> Result<Response>>(1)
            callback()
        }

        recommendationNetworkClient = RecommendationNetworkClient(
            mockGenericNetworkClient,
            mockStringStorage,
            mockRequestContext,
            mockUserAgentProvider,
            mockContactTokenRefresher,
            sdkLogger = mock(MockMode.autofill)
        )
        everySuspend { mockUserAgentProvider.provide() } returns USER_AGENT_STRING
    }

    @Test
    fun send_shouldSendRequest_withScarabHeaders_whenStringStorageIsNotEmpty_andNotStoreHeaders_whenResponseHeadersAreTheSame() =
        runTest {
            every { mockStringStorage.get(SCARAB_VISITOR_HEADER) } returns VISITOR_VALUE
            every { mockStringStorage.get(SCARAB_SESSION_HEADER) } returns SESSION_VALUE
            every { mockStringStorage.get(SCARAB_USER_PROFILE_HEADER) } returns USER_PROFILE_VALUE
            every { mockStringStorage.get(SCARAB_FORCE_COHORT_HEADER) } returns FORCE_COHORT_VALUE
            every { mockRequestContext.contactToken } returns CONTACT_TOKEN
            val expectedHeaders = mapOf(
                CONTACT_TOKEN_HEADER to CONTACT_TOKEN,
                USER_AGENT_HEADER to USER_AGENT_STRING,
                SCARAB_VISITOR_HEADER to VISITOR_VALUE,
                SCARAB_SESSION_HEADER to SESSION_VALUE,
                SCARAB_USER_PROFILE_HEADER to USER_PROFILE_VALUE,
                SCARAB_FORCE_COHORT_HEADER to FORCE_COHORT_VALUE
            )
            val requestCaptor = slot<UrlRequest>()
            everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITH_SCARAB_HEADERS

            recommendationNetworkClient.send(REQUEST)

            verify {
                requestCaptor.get().headers shouldBe expectedHeaders
                mockRequestContext.contactToken
            }
            verify(VerifyMode.exactly(0)) {
                mockStringStorage.put(SCARAB_VISITOR_HEADER, VISITOR_VALUE)
                mockStringStorage.put(SCARAB_SESSION_HEADER, SESSION_VALUE)
                mockStringStorage.put(SCARAB_USER_PROFILE_HEADER, USER_PROFILE_VALUE)
                mockStringStorage.put(SCARAB_FORCE_COHORT_HEADER, FORCE_COHORT_VALUE)
            }
            verifySuspend { mockUserAgentProvider.provide() }
        }

    @Test
    fun send_shouldSendRequest_withScarabHeaders_whenStringStorageIsNotEmpty_andStoreHeaders_whenResponseHeadersAreDifferent() =
        runTest {
            every { mockStringStorage.get(SCARAB_VISITOR_HEADER) } returns VISITOR_VALUE
            every { mockStringStorage.get(SCARAB_SESSION_HEADER) } returns SESSION_VALUE
            every { mockStringStorage.get(SCARAB_USER_PROFILE_HEADER) } returns USER_PROFILE_VALUE
            every { mockStringStorage.get(SCARAB_FORCE_COHORT_HEADER) } returns FORCE_COHORT_VALUE
            every { mockRequestContext.contactToken } returns CONTACT_TOKEN
            val expectedHeaders = mapOf(
                CONTACT_TOKEN_HEADER to CONTACT_TOKEN,
                USER_AGENT_HEADER to USER_AGENT_STRING,
                SCARAB_VISITOR_HEADER to VISITOR_VALUE,
                SCARAB_SESSION_HEADER to SESSION_VALUE,
                SCARAB_USER_PROFILE_HEADER to USER_PROFILE_VALUE,
                SCARAB_FORCE_COHORT_HEADER to FORCE_COHORT_VALUE
            )
            val requestCaptor = slot<UrlRequest>()
            everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITH_DIFFERENT_SCARAB_HEADERS

            recommendationNetworkClient.send(REQUEST)

            verify {
                requestCaptor.get().headers shouldBe expectedHeaders
                mockRequestContext.contactToken
                mockStringStorage.put(SCARAB_VISITOR_HEADER, OTHER_VISITOR_VALUE)
                mockStringStorage.put(SCARAB_SESSION_HEADER, OTHER_SESSION_VALUE)
                mockStringStorage.put(SCARAB_USER_PROFILE_HEADER, OTHER_USER_PROFILE_VALUE)
                mockStringStorage.put(SCARAB_FORCE_COHORT_HEADER, OTHER_FORCE_COHORT_VALUE)
            }
            verifySuspend { mockUserAgentProvider.provide() }
        }

    @Test
    fun send_shouldNotClearStoredHeaders_whenResponseHeadersAreMissing() =
        runTest {
            every { mockStringStorage.get(SCARAB_VISITOR_HEADER) } returns VISITOR_VALUE
            every { mockStringStorage.get(SCARAB_SESSION_HEADER) } returns SESSION_VALUE
            every { mockStringStorage.get(SCARAB_USER_PROFILE_HEADER) } returns USER_PROFILE_VALUE
            every { mockStringStorage.get(SCARAB_FORCE_COHORT_HEADER) } returns FORCE_COHORT_VALUE
            every { mockRequestContext.contactToken } returns CONTACT_TOKEN
            val requestCaptor = slot<UrlRequest>()
            everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITHOUT_SCARAB_HEADERS

            recommendationNetworkClient.send(REQUEST)

            verify(VerifyMode.exactly(0)) {
                mockStringStorage.put(SCARAB_VISITOR_HEADER, any())
                mockStringStorage.put(SCARAB_SESSION_HEADER, any())
                mockStringStorage.put(SCARAB_USER_PROFILE_HEADER, any())
                mockStringStorage.put(SCARAB_FORCE_COHORT_HEADER, any())
            }
            verifySuspend { mockUserAgentProvider.provide() }
        }

    @Test
    fun send_shouldSendRequest_withContactTokenAndUserAgentHeadersOnly_whenStringStorageIsEmpty_andStoreHeaders_whenResponseIncludesThem() =
        runTest {
            every { mockStringStorage.get(any()) } returns null
            every { mockRequestContext.contactToken } returns CONTACT_TOKEN
            val expectedHeaders =
                mapOf(CONTACT_TOKEN_HEADER to CONTACT_TOKEN, USER_AGENT_HEADER to USER_AGENT_STRING)
            val requestCaptor = slot<UrlRequest>()
            everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITH_SCARAB_HEADERS

            recommendationNetworkClient.send(REQUEST)

            verify {
                requestCaptor.get().headers shouldBe expectedHeaders
                mockRequestContext.contactToken
                mockStringStorage.put(SCARAB_VISITOR_HEADER, VISITOR_VALUE)
                mockStringStorage.put(SCARAB_SESSION_HEADER, SESSION_VALUE)
                mockStringStorage.put(SCARAB_USER_PROFILE_HEADER, USER_PROFILE_VALUE)
                mockStringStorage.put(SCARAB_FORCE_COHORT_HEADER, FORCE_COHORT_VALUE)
            }
            verifySuspend { mockUserAgentProvider.provide() }
        }

    @Test
    fun send_shouldSendRequest_withContactTokenAndUserAgentHeadersOnly_whenStringStorageIsEmptyString_andNotStoreHeaders_whenResponseExcludesThem() =
        runTest {
            every { mockStringStorage.get(any()) } returns ""
            every { mockRequestContext.contactToken } returns CONTACT_TOKEN
            val expectedHeaders =
                mapOf(CONTACT_TOKEN_HEADER to CONTACT_TOKEN, USER_AGENT_HEADER to USER_AGENT_STRING)
            val requestCaptor = slot<UrlRequest>()
            everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITHOUT_SCARAB_HEADERS

            recommendationNetworkClient.send(REQUEST)

            verify {
                requestCaptor.get().headers shouldBe expectedHeaders
            }
            verify(VerifyMode.exactly(0)) {
                mockStringStorage.put(SCARAB_VISITOR_HEADER, any())
                mockStringStorage.put(SCARAB_SESSION_HEADER, any())
                mockStringStorage.put(SCARAB_USER_PROFILE_HEADER, any())
                mockStringStorage.put(SCARAB_FORCE_COHORT_HEADER, any())
            }
            verifySuspend { mockUserAgentProvider.provide() }
        }

    @Test
    fun send_shouldOnlyAddExistingRecommendationHeaders() = runTest {
        every { mockStringStorage.get(SCARAB_VISITOR_HEADER) } returns VISITOR_VALUE
        every { mockStringStorage.get(SCARAB_SESSION_HEADER) } returns null
        every { mockStringStorage.get(SCARAB_USER_PROFILE_HEADER) } returns null
        every { mockStringStorage.get(SCARAB_FORCE_COHORT_HEADER) } returns null
        every { mockRequestContext.contactToken } returns CONTACT_TOKEN
        val expectedHeaders = mapOf(
            CONTACT_TOKEN_HEADER to CONTACT_TOKEN,
            USER_AGENT_HEADER to USER_AGENT_STRING,
            SCARAB_VISITOR_HEADER to VISITOR_VALUE
        )
        val requestCaptor = slot<UrlRequest>()
        everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns SUCCESS_RESPONSE_WITHOUT_SCARAB_HEADERS

        recommendationNetworkClient.send(REQUEST)

        verify {
            requestCaptor.get().headers shouldBe expectedHeaders
            mockRequestContext.contactToken
        }
        verifySuspend { mockUserAgentProvider.provide() }
    }

    @Test
    fun send_shouldReturnResultFailure_whenContactTokenIsMissing() = runTest {
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
    fun send_shouldDelegateToContactTokenRefresher() = runTest {
        every { mockRequestContext.contactToken } returns CONTACT_TOKEN
        every { mockStringStorage.get(any()) } returns null
        everySuspend { mockGenericNetworkClient.send(any()) } returns SUCCESS_RESPONSE_WITHOUT_SCARAB_HEADERS

        recommendationNetworkClient.send(REQUEST)

        verifySuspend { mockContactTokenRefresher.executeWithTokenRefresh(any(), any()) }
    }
}
