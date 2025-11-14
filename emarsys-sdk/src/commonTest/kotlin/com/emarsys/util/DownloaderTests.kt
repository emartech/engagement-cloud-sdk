package com.emarsys.util

import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.log.Logger
import com.emarsys.core.util.Downloader
import com.emarsys.core.util.DownloaderApi
import com.emarsys.testutil.MockHttpClientFactory
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DownloaderTests {
    private companion object {
        const val URL = "testUrl"
    }
    private lateinit var client: HttpClient
    private lateinit var mockFileCache: FileCacheApi
    private lateinit var mockLogger: Logger
    private lateinit var downloader: DownloaderApi

    @BeforeTest
    fun setup() {
        mockFileCache = mock(MockMode.autofill)
        every { mockFileCache.get(any()) } returns null
        mockLogger = mock(MockMode.autofill)
    }

    @Test
    fun download_shouldReturn_downloadedByteArray() = runTest {
        val testImage = "testImageStringRepresentation"
        client = MockHttpClientFactory.create(body = testImage)
        downloader = Downloader(client, mockFileCache, mockLogger)

        val result = downloader.download(URL)

        result shouldBe testImage.encodeToByteArray()
    }

    @Test
    fun download_shouldReturn_theCachedArray_ifAvailable() = runTest {
        val testImage = "testImageStringRepresentation"
        val cachedTestImage = "cachedTestImageStringRepresentation"
        client = MockHttpClientFactory.create(body = testImage)
        every { mockFileCache.get(any()) } returns cachedTestImage.encodeToByteArray()

        downloader = Downloader(client, mockFileCache, mockLogger)

        val result = downloader.download(URL)

        result shouldBe cachedTestImage.encodeToByteArray()
    }

    @Test
    fun download_shouldReturn_theFallbackValue_ifAvailable_andDownload_fails_andNoCacheIsAvailable() = runTest {
        val fallbackTestImage = "fallbackTestImageStringRepresentation".encodeToByteArray()

        client = MockHttpClientFactory.create(responseStatus = HttpStatusCode.InternalServerError, body = "")

        every { mockFileCache.get(any()) } returns null

        downloader = Downloader(client, mockFileCache, mockLogger)

        val result = downloader.download(URL, fallbackTestImage)

        result shouldBe fallbackTestImage

        verify(VerifyMode.exactly(0)) { mockFileCache.cache(any(), any()) }
    }

    @Test
    fun download_shouldReturn_null_ifDownload_fails_andNoCacheIsAvailable_andNoFallbackIsProvided() = runTest {
        client = MockHttpClientFactory.create(responseStatus = HttpStatusCode.InternalServerError, body = "")

        every { mockFileCache.get(any()) } returns null

        downloader = Downloader(client, mockFileCache, mockLogger)

        val result = downloader.download(URL, null)

        result shouldBe null

        verify(VerifyMode.exactly(0)) { mockFileCache.cache(any(), any()) }
    }

    @Test
    fun download_shouldReturn_theFallbackValue_ifReadingFromCacheFails() = runTest {
        val fallbackTestImage = "fallbackTestImageStringRepresentation".encodeToByteArray()

        client = MockHttpClientFactory.create(responseStatus = HttpStatusCode.InternalServerError, body = "")

        every { mockFileCache.get(any()) } throws Exception("this failed")

        downloader = Downloader(client, mockFileCache, mockLogger)

        val result = downloader.download(URL, fallbackTestImage)

        result shouldBe fallbackTestImage
    }

    @Test
    fun download_shouldReturn_null_ifReadingFromCacheFails_andNoFallbackIsProvided() = runTest {
        client = MockHttpClientFactory.create(responseStatus = HttpStatusCode.InternalServerError, body = "")

        every { mockFileCache.get(any()) } throws Exception("this failed")

        downloader = Downloader(client, mockFileCache, mockLogger)

        val result = downloader.download(URL, null)

        result shouldBe null
    }
}