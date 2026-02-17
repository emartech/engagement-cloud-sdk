package com.sap.ec.iosNotificationService.networking

import com.sap.ec.iosNotificationService.file.FileSmith
import com.sap.ec.iosNotificationService.provider.SessionProvider
import com.sap.ec.iosNotificationService.provider.UUIDProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
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
        val url = NSURL(string = "https://gist.githubusercontent.com/LasOri/9a2c4eda66ca6a31f7b5afb5e20ba4c1/raw/7d0935addb36e01861a534ef0adc79d9c8a32e53/test.txt")

        val result = downloader.downloadFile(url)

        NSFileManager.defaultManager().fileExistsAtPath(result!!.path()!!) shouldBe true
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    @Test
    fun download_shouldReturn_withData_thatContainsTheDownloadedContent() = runTest {
        val url = NSURL(string = "https://gist.githubusercontent.com/LasOri/9a2c4eda66ca6a31f7b5afb5e20ba4c1/raw/7d0935addb36e01861a534ef0adc79d9c8a32e53/test.txt")

        val result = downloader.downloadData(url)

        result shouldBe ("TEST" as NSString).dataUsingEncoding(NSUTF8StringEncoding)
    }

}
