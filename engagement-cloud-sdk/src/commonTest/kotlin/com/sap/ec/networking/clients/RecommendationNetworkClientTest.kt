package com.sap.ec.networking.clients

import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.VISITOR_ID_COOKIE_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.XP_COOKIE_KEY
import com.sap.ec.networking.RecommendationNetworkClient
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
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
    private lateinit var mockStringStorageApi: StringStorageApi

    private companion object {
        private const val XP_VALUE = "testXpValue"
        private const val CVD_VALUE = "testCvdValue"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockGenericNetworkClient = mock(MockMode.autofill)
        mockStringStorageApi = mock(MockMode.autofill)
        recommendationNetworkClient =
            RecommendationNetworkClient(mockGenericNetworkClient, mockStringStorageApi)

    }

    @Test
    fun test_send_shouldNotSendAnyPredictHeader_whenStringStorageIsEmpty() = runTest {
        val testRequest = UrlRequest(url = Url("https://test.com"), method = HttpMethod.Get)
        every { mockStringStorageApi.get(XP_COOKIE_KEY) } returns null
        every { mockStringStorageApi.get(VISITOR_ID_COOKIE_KEY) } returns null

        recommendationNetworkClient.send(testRequest)

        verifySuspend {
            mockGenericNetworkClient.send(testRequest)
            mockStringStorageApi.get(XP_COOKIE_KEY)
            mockStringStorageApi.get(VISITOR_ID_COOKIE_KEY)
        }
    }

    @Test
    fun test_send_shouldNotSendAnyPredictHeader_whenStringStorageIsEmptyString() = runTest {
        val testRequest = UrlRequest(url = Url("https://test.com"), method = HttpMethod.Get)
        every { mockStringStorageApi.get(XP_COOKIE_KEY) } returns ""
        every { mockStringStorageApi.get(VISITOR_ID_COOKIE_KEY) } returns ""

        recommendationNetworkClient.send(testRequest)

        verifySuspend {
            mockGenericNetworkClient.send(testRequest)
            mockStringStorageApi.get(XP_COOKIE_KEY)
            mockStringStorageApi.get(VISITOR_ID_COOKIE_KEY)
        }
    }

    @Test
    fun test_send_shouldNotSendAnyPredictHeader_whenStringStorageIsNotEmpty() = runTest {
        val testRequest = UrlRequest(url = Url("https://test.com"), method = HttpMethod.Get)
        every { mockStringStorageApi.get(XP_COOKIE_KEY) } returns XP_VALUE
        every { mockStringStorageApi.get(VISITOR_ID_COOKIE_KEY) } returns CVD_VALUE
        val expectedHeaders = mapOf("Cookie" to "xp=$XP_VALUE;cdv=$CVD_VALUE")
        val requestCaptor = slot<UrlRequest>()
        everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns Result.success(
            Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = headersOf("1", "2"),
                bodyAsText = ""
            )
        )

        recommendationNetworkClient.send(testRequest)

        verifySuspend {
            requestCaptor.get().headers shouldBe expectedHeaders
            mockStringStorageApi.get(XP_COOKIE_KEY)
            mockStringStorageApi.get(VISITOR_ID_COOKIE_KEY)
        }
    }

    @Test
    fun test_send_shouldOnlyAddExistingPredictHeaders() = runTest {
        val testRequest = UrlRequest(url = Url("https://test.com"), method = HttpMethod.Get)
        every { mockStringStorageApi.get(XP_COOKIE_KEY) } returns XP_VALUE
        every { mockStringStorageApi.get(VISITOR_ID_COOKIE_KEY) } returns ""
        val expectedHeaders = mapOf("Cookie" to "xp=$XP_VALUE;")
        val requestCaptor = slot<UrlRequest>()
        everySuspend { mockGenericNetworkClient.send(capture(requestCaptor)) } returns Result.success(
            Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = headersOf("1", "2"),
                bodyAsText = ""
            )
        )

        recommendationNetworkClient.send(testRequest)

        verifySuspend {
            requestCaptor.get().headers shouldBe expectedHeaders
            mockStringStorageApi.get(XP_COOKIE_KEY)
            mockStringStorageApi.get(VISITOR_ID_COOKIE_KEY)
        }
    }
}