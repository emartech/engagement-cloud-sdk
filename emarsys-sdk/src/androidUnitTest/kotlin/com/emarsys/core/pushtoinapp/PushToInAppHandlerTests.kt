package com.emarsys.core.pushtoinapp

import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PushToInAppHandlerTests {
    private companion object {
        const val CAMPAIGN_ID = "testCampaignId"
        const val INAPP_URL = "inapp url"
        const val HTML = "inapp content"
    }

    private lateinit var pushToInAppHandler: PushToInAppHandler
    private lateinit var mockInAppHandler: InAppHandlerApi
    private lateinit var mockInAppDownloader: InAppDownloaderApi

    @Before
    fun setup() {
        mockInAppDownloader = mockk(relaxed = true)
        mockInAppHandler = mockk(relaxed = true)

        pushToInAppHandler = PushToInAppHandler(
            mockInAppDownloader,
            mockInAppHandler
        )
    }

    @Test
    fun handle_shouldCallHandle_onInAppHandler_ifHtmlIsNotEmpty() = runTest {
        val testInappAction =
            InternalPushToInappActionModel(CAMPAIGN_ID, INAPP_URL, HTML, false)

        pushToInAppHandler.handle(testInappAction)

        coVerify { mockInAppHandler.handle(HTML) }
    }

    @Test
    fun handle_shouldNotCallHandle_onInAppHandler_ifHtmlIsEmpty() = runTest {
        val testInAppHtml = ""
        val testInappAction =
            InternalPushToInappActionModel(CAMPAIGN_ID, INAPP_URL, testInAppHtml, false)

        pushToInAppHandler.handle(testInappAction)

        coVerify(exactly = 0) { mockInAppHandler.handle(any()) }
    }

    @Test
    fun handle_shouldDownloadHtml_andCallHandle_ifDownloadSucceeds() = runTest {
        val testInAppHtml: String? = null
        val testInappAction =
            InternalPushToInappActionModel(CAMPAIGN_ID, INAPP_URL, testInAppHtml, false)

        coEvery { mockInAppDownloader.download(INAPP_URL) } returns HTML

        pushToInAppHandler.handle(testInappAction)

        coVerify { mockInAppDownloader.download(INAPP_URL) }
        coVerify { mockInAppHandler.handle(HTML) }
    }

    @Test
    fun handle_shouldDownloadHtml_andNotCallHandle_ifDownloadFails() = runTest {
        val testInAppHtml: String? = null
        val testInappAction =
            InternalPushToInappActionModel(CAMPAIGN_ID, INAPP_URL, testInAppHtml, false)

        coEvery { mockInAppDownloader.download(INAPP_URL) } returns null

        pushToInAppHandler.handle(testInappAction)

        coVerify { mockInAppDownloader.download(INAPP_URL) }
        coVerify(exactly = 0) { mockInAppHandler.handle(any()) }
    }
}