package com.emarsys.iosNotificationService.networking

import com.emarsys.iosNotificationService.file.FileSmith
import com.emarsys.iosNotificationService.provider.SessionProvider
import com.emarsys.iosNotificationService.provider.UUIDProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import kotlin.test.BeforeTest
import kotlin.test.Test

class DownloaderTests {

    private lateinit var mockFileSmith: FileSmith
    private lateinit var urlSession: NSURLSession
    private lateinit var downloader: Downloader

    @BeforeTest
    fun setup() = runTest {
        mockFileSmith = FileSmith(UUIDProvider())
        urlSession = SessionProvider().provide()
        downloader = Downloader(urlSession, mockFileSmith)
    }

    @Test
    fun download_shouldReturn_withUrl_thatContainsTheDownloadedContent() = runTest {
        val url = NSURL(string = "https://mobile-sdk-config-staging.gservice.emarsys.com/testing/Emarsys.png")

        val result = downloader.download(url)

        NSFileManager.defaultManager().fileExistsAtPath(result!!.path()!!) shouldBe true
    }

}
