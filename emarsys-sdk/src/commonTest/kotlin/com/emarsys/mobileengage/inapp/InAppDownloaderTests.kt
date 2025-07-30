package com.emarsys.mobileengage.inapp

import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.networking.clients.event.model.ContentCampaign
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
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
import kotlinx.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test

class InAppDownloaderTests {
    private companion object {
        const val TEST_CONTENT = "test content"
        const val TRACKING_INFO = "testTrackingInfo"
        const val URL = "https://test.com"
        val TEST_URL = Url(URL)
        val testRequest = UrlRequest(TEST_URL, HttpMethod.Post)
        val testContentCampaign = ContentCampaign(InAppType.OVERLAY, TRACKING_INFO, TEST_CONTENT)
        val response: Response = Response(
            testRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            JsonUtil.json.encodeToString(testContentCampaign)
        )
    }

    private lateinit var inAppDownloader: InAppDownloader
    private lateinit var mockEmarsysNetworkClient: NetworkClientApi
    private lateinit var mockLogger: Logger

    @BeforeTest
    fun setup() {

        mockEmarsysNetworkClient = mock()
        mockLogger = mock(MockMode.autofill)
        inAppDownloader = InAppDownloader(mockEmarsysNetworkClient, mockLogger)
    }

    @Test
    fun download_shouldReturn_theResponse_asInAppMessage() = runTest {
        everySuspend { mockEmarsysNetworkClient.send(testRequest) } returns response

        val result = inAppDownloader.download(URL)

        result?.type shouldBe InAppType.OVERLAY
        result?.trackingInfo shouldBe TRACKING_INFO
        result?.content shouldBe TEST_CONTENT
    }

    @Test
    fun download_shouldReturn_null_ifDownloadReturnsFailedRequestException() = runTest {
        val testException = FailedRequestException(response)
        everySuspend { mockEmarsysNetworkClient.send(testRequest) } throws testException

        val result = inAppDownloader.download(URL)

        result shouldBe null

        verifySuspend {
            mockLogger.error(any(), testException)
        }
    }

    @Test
    fun download_shouldReturn_null_ifDownloadReturnsIoException() = runTest {
        val testException = IOException("Download failed")
        everySuspend { mockEmarsysNetworkClient.send(testRequest) } throws testException

        val result = inAppDownloader.download(URL)

        result shouldBe null

        verifySuspend {
            mockLogger.error(any(), testException)
        }
    }
}