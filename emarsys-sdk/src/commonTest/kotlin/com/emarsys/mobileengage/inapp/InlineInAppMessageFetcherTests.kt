package com.emarsys.mobileengage.inapp

import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
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
    private lateinit var fetcher: InlineInAppMessageFetcher

    private val testViewId = "testViewId"
    private val testUrl = Url("https://example.com/inline-messages")

    @BeforeTest
    fun setup() {
        mockNetworkClient = mock(MockMode.autoUnit)
        mockUrlFactory = mock(MockMode.autoUnit)
        fetcher = InlineInAppMessageFetcher(mockNetworkClient, mockUrlFactory, Json { ignoreUnknownKeys = true })

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
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result!!.type shouldBe InAppType.INLINE
        result.trackingInfo shouldBe "campaign123"
        result.content shouldBe "<div>Content</div>"

        verifySuspend {
            mockUrlFactory.create(EmarsysUrlType.FetchInlineInAppMessages)
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
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
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
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
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
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = responseJson
        )

        everySuspend { mockNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result shouldBe null
    }

    @Test
    fun fetch_shouldReturnNull_whenNetworkRequestFails() = runTest {
        everySuspend { mockNetworkClient.send(any()) } returns Result.failure(Exception("Network error"))

        val result = fetcher.fetch(testViewId)

        result shouldBe null
    }

    @Test
    fun fetch_shouldReturnNull_whenResponseDecodingFails() = runTest {
        val response = Response(
            originalRequest = UrlRequest(testUrl, HttpMethod.Post),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = "invalid json"
        )

        everySuspend { mockNetworkClient.send(any()) } returns Result.success(response)

        val result = fetcher.fetch(testViewId)

        result shouldBe null
    }
}
