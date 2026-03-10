package com.sap.ec.mobileengage.inapp.networking.download

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.mobileengage.inapp.networking.models.EmbeddedMessagingRichContentUrlHolder
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
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

    private lateinit var mockECNetworkClient: NetworkClientApi
    private lateinit var mockGenericNetworkClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockLogger: Logger
    private lateinit var fetcher: InlineInAppMessageFetcher
    private lateinit var json: Json

    private val testViewId = "testViewId"
    private val testTrackingInfo = "testTrackingInfo"
    private val testUrl = Url("https://sap.com/inline-messages")

    @BeforeTest
    fun setup() {
        mockECNetworkClient = mock(MockMode.autoUnit)
        mockGenericNetworkClient = mock(MockMode.autoUnit)
        mockUrlFactory = mock(MockMode.autoUnit)
        mockLogger = mock(MockMode.autofill)
        json = JsonUtil.json
        fetcher = InlineInAppMessageFetcher(
            mockGenericNetworkClient,
            mockECNetworkClient,
            mockUrlFactory,
            json,
            mockLogger
        )

        everySuspend { mockUrlFactory.create(any()) } returns testUrl
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
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result!!.type shouldBe InAppType.INLINE
        result.trackingInfo shouldBe "campaign123"
        result.content shouldBe "<div>Content</div>"

        verifySuspend {
            mockUrlFactory.create(ECUrlType.FetchInlineInAppMessages)
            mockECNetworkClient.send(any())
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
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(response)

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
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(response)

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
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result shouldBe null
    }

    @Test
    fun fetch_shouldReturnNull_whenResponseIsEmpty() = runTest {
        val responseJson = "".trimIndent()

        val response = Response(
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.NoContent,
            headers = Headers.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        verifySuspend {
            mockLogger.debug("Received 204 No Content response for viewId: $testViewId")
        }
        result shouldBe null
    }

    @Test
    fun fetch_shouldReturnNull_whenNetworkRequestFails() = runTest {
        val testException = Exception("Network error")
        everySuspend { mockECNetworkClient.send(any()) } returns Result.failure(testException)

        val result = fetcher.fetch(testViewId)

        result shouldBe null

        verifySuspend {
            mockLogger.error(any(), testException)
        }
    }

    @Test
    fun fetch_shouldReturnNull_whenResponseDecodingFails() = runTest {
        val response = Response(
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = "invalid json"
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result shouldBe null

        verifySuspend {
            mockLogger.error(any(), any<Throwable>())
        }
    }

    @Test
    fun fetchByUrl_shouldReturnMessage_whenResponseIsSuccessful() = runTest {
        val testRichContentUrl = Url("https://fetchUrlFromHere.com/url")
        val contentUrl = "https://sap.com/inline-content"
        val testUrlHolder = EmbeddedMessagingRichContentUrlHolder(contentUrl)
        val urlHolderJson = json.encodeToString(testUrlHolder)
        val htmlContent = "<div>Inline Content</div>"

        val urlHolderResponse = Response(
            originalRequest = UrlRequest(testRichContentUrl, HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = urlHolderJson
        )

        val richContentResponse = Response(
            originalRequest = UrlRequest(Url(contentUrl), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = htmlContent
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(urlHolderResponse)
        everySuspend { mockGenericNetworkClient.send(any()) } returns Result.success(
            richContentResponse
        )

        val result = fetcher.fetch(testRichContentUrl, testTrackingInfo)

        result!!.type shouldBe InAppType.INLINE
        result.trackingInfo shouldBe testTrackingInfo
        result.content shouldBe htmlContent

        verifySuspend(VerifyMode.exactly(1)) {
            mockECNetworkClient.send(any())
        }
        verifySuspend(VerifyMode.exactly(1)) {
            mockGenericNetworkClient.send(any())
        }
    }

    @Test
    fun fetchByUrl_shouldReturnNull_whenRichContentUrlResponseCannotBeParsed_asUrlHolder() = runTest {
        val testRichContentUrl = Url("https://example.com/url")
        val urlHolderJson = "can not parse this"

        val urlHolderResponse = Response(
            originalRequest = UrlRequest(testRichContentUrl, HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = urlHolderJson
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(urlHolderResponse)

        val result = fetcher.fetch(testRichContentUrl, testTrackingInfo)

        result shouldBe null

        verifySuspend(VerifyMode.exactly(1)) {
            mockECNetworkClient.send(any())
        }
    }

    @Test
    fun fetchByUrl_shouldReturnNull_whenRichContentResponse_containsInvalidUrl() = runTest {
        val testRichContentUrl = Url("https://example.com/url")
        val contentUrl = "this is not a valid url %!=\\@"
        val testUrlHolder = EmbeddedMessagingRichContentUrlHolder(contentUrl)
        val urlHolderJson = json.encodeToString(testUrlHolder)

        val urlHolderResponse = Response(
            originalRequest = UrlRequest(testRichContentUrl, HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = urlHolderJson
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(urlHolderResponse)

        val result = fetcher.fetch(testRichContentUrl, testTrackingInfo)

        result shouldBe null

        verifySuspend(VerifyMode.exactly(1)) {
            mockECNetworkClient.send(any())
        }
    }

    @Test
    fun fetchByUrl_shouldReturnNull_whenRichContentFetchingFails() = runTest {
        val testRichContentUrl = Url("https://fetchUrlFromHere.com/url")
        val contentUrl = "https://sap.com/inline-content"
        val testUrlHolder = EmbeddedMessagingRichContentUrlHolder(contentUrl)
        val urlHolderJson = json.encodeToString(testUrlHolder)

        val urlHolderResponse = Response(
            originalRequest = UrlRequest(testRichContentUrl, HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = urlHolderJson
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(urlHolderResponse)
        everySuspend { mockGenericNetworkClient.send(any()) } returns Result.failure(Exception("failure"))

        val result = fetcher.fetch(testRichContentUrl, testTrackingInfo)

        result shouldBe null

        verifySuspend(VerifyMode.exactly(1)) {
            mockECNetworkClient.send(any())
        }
        verifySuspend(VerifyMode.exactly(1)) {
            mockGenericNetworkClient.send(any())
        }
    }

    @Test
    fun fetchByUrl_shouldReturnNull_whenNetworkRequestFails() = runTest {
        val contentUrl = Url("https://sap.com/inline-content")
        val testException = Exception("Network error")

        everySuspend { mockECNetworkClient.send(any()) } returns Result.failure(testException)

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
            originalRequest = UrlRequest(contentUrl, HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = ""
        )

        everySuspend { mockECNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(contentUrl, testTrackingInfo)

        result shouldBe null
    }
}