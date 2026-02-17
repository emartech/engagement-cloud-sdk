package com.sap.ec.mobileengage.inapp.networking.download

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class InlineInAppMessageFetcherTests {

    private lateinit var mockNetworkClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockLogger: Logger
    private lateinit var fetcher: InlineInAppMessageFetcher

    private val testViewId = "testViewId"
    private val testTrackingInfo = "testTrackingInfo"
    private val testUrl = Url("https://sap.com/inline-messages")

    @BeforeTest
    fun setup() {
        mockNetworkClient = mock(MockMode.autoUnit)
        mockUrlFactory = mock(MockMode.autoUnit)
        mockLogger = mock(MockMode.autofill)
        fetcher = InlineInAppMessageFetcher(
            mockNetworkClient,
            mockUrlFactory,
            Json { ignoreUnknownKeys = true },
            mockLogger
        )

        every { mockUrlFactory.create(any()) } returns testUrl
    }

    @Test
    fun fetch_shouldReturnMessage_whenViewIdMatchesAndContentIsNotEmpty() = runTest {
        val responseJson = """
            {
              "inlineMessages": [
                {
                  "type": "inline",
                  "trackingInfo": "campaign123",
                  "content": "<div>Content</div>",
                  "viewId": "$testViewId"
                }
              ]
            }
        """.trimIndent()

        val response = Response(
            originalRequest = UrlRequest(testUrl, HttpMethod.Companion.Post),
            status = HttpStatusCode.Companion.OK,
            headers = Headers.Companion.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result!!.type shouldBe InAppType.INLINE
        result.trackingInfo shouldBe "campaign123"
        result.content shouldBe "<div>Content</div>"

        verifySuspend {
            mockUrlFactory.create(ECUrlType.FetchInlineInAppMessages)
            mockNetworkClient.send(any())
        }
    }

    @Test
    fun fetch_shouldParseOverlayType_correctly() = runTest {
        val responseJson = """
            {
              "inlineMessages": [
                {
                  "type": "overlay",
                  "trackingInfo": "campaign456",
                  "content": "<div>Overlay Content</div>",
                  "viewId": "$testViewId"
                }
              ]
            }
        """.trimIndent()

        val response = Response(
            originalRequest = UrlRequest(testUrl, HttpMethod.Companion.Post),
            status = HttpStatusCode.Companion.OK,
            headers = Headers.Companion.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result!!.type shouldBe InAppType.OVERLAY
    }

    @Test
    fun fetch_shouldReturnNull_whenViewIdDoesNotMatch() = runTest {
        val responseJson = """
            {
              "inlineMessages": [
                {
                  "type": "inline",
                  "trackingInfo": "campaign123",
                  "content": "<div>Content</div>",
                  "viewId": "otherViewId"
                }
              ]
            }
        """.trimIndent()

        val response = Response(
            originalRequest = UrlRequest(testUrl, HttpMethod.Companion.Post),
            status = HttpStatusCode.Companion.OK,
            headers = Headers.Companion.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result shouldBe null
    }

    @Test
    fun fetch_shouldReturnNull_whenContentIsEmpty() = runTest {
        val responseJson = """
            {
              "inlineMessages": [
                {
                  "type": "inline",
                  "trackingInfo": "campaign123",
                  "content": "",
                  "viewId": "$testViewId"
                }
              ]
            }
        """.trimIndent()

        val response = Response(
            originalRequest = UrlRequest(testUrl, HttpMethod.Companion.Post),
            status = HttpStatusCode.Companion.OK,
            headers = Headers.Companion.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result shouldBe null
    }

    @Test
    fun fetch_shouldReturnNull_whenNetworkRequestFails() = runTest {
        val testException = Exception("Network error")
        everySuspend { mockNetworkClient.send(any()) } returns Result.failure(testException)

        val result = fetcher.fetch(testViewId)

        result shouldBe null

        verifySuspend {
            mockLogger.error(any(), testException)
        }
    }

    @Test
    fun fetch_shouldReturnNull_whenResponseDecodingFails() = runTest {
        val response = Response(
            originalRequest = UrlRequest(testUrl, HttpMethod.Companion.Post),
            status = HttpStatusCode.Companion.OK,
            headers = Headers.Companion.Empty,
            bodyAsText = "invalid json"
        )

        everySuspend { mockNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result shouldBe null

        verifySuspend {
            mockLogger.error(any(), any<Throwable>())
        }
    }

    @Test
    fun fetchByUrl_shouldReturnMessage_whenResponseIsSuccessful() = runTest {
        val contentUrl = Url("https://sap.com/inline-content")
        val htmlContent = "<div>Inline Content</div>"

        val response = Response(
            originalRequest = UrlRequest(contentUrl, HttpMethod.Companion.Get),
            status = HttpStatusCode.Companion.OK,
            headers = Headers.Companion.Empty,
            bodyAsText = htmlContent
        )

        everySuspend { mockNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(contentUrl, testTrackingInfo)

        result!!.type shouldBe InAppType.INLINE
        result.trackingInfo shouldBe testTrackingInfo
        result.content shouldBe htmlContent

        verifySuspend {
            mockNetworkClient.send(any())
        }
    }

    @Test
    fun fetchByUrl_shouldReturnNull_whenNetworkRequestFails() = runTest {
        val contentUrl = Url("https://sap.com/inline-content")
        val testException = Exception("Network error")

        everySuspend { mockNetworkClient.send(any()) } returns Result.failure(testException)

        val result = fetcher.fetch(contentUrl, testTrackingInfo)

        result shouldBe null

        verifySuspend {
            mockLogger.error(any(), testException)
        }
    }

    @Test
    fun fetchByUrl_shouldReturnNull_whenResponseBodyIsEmpty() = runTest {
        val contentUrl = Url("https://sap.com/inline-content")

        val response = Response(
            originalRequest = UrlRequest(contentUrl, HttpMethod.Companion.Get),
            status = HttpStatusCode.Companion.OK,
            headers = Headers.Companion.Empty,
            bodyAsText = ""
        )

        everySuspend { mockNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(contentUrl, testTrackingInfo)

        result shouldBe null
    }
}