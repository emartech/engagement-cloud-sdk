package com.emarsys.mobileengage.inapp

import com.emarsys.core.util.DownloaderApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class InAppDownloaderTests {
    private companion object {
        const val TEST_URL = "https://test.com"
        const val TEST_CONTENT = "test content"
        val testDownloadedBytes = TEST_CONTENT.toByteArray(Charsets.UTF_8)
    }

    private lateinit var inAppDownloader: InAppDownloader
    private lateinit var mockDownloader: DownloaderApi

    @BeforeTest
    fun setup() {
        mockDownloader = mock()
        inAppDownloader = InAppDownloader(mockDownloader)
    }

    @Test
    fun download_shouldReturn_downloadedContent_asString() = runTest {
        everySuspend { mockDownloader.download(TEST_URL) } returns testDownloadedBytes

        val result = inAppDownloader.download(TEST_URL)

        result shouldBe TEST_CONTENT
    }

    @Test
    fun download_shouldReturn_null_ifDownloadFails() = runTest {
        everySuspend { mockDownloader.download(TEST_URL) } returns null

        val result = inAppDownloader.download(TEST_URL)

        result shouldBe null
    }
}