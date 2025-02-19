package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebPushToInAppHandlerTests {
    private companion object {
        const val URL = "url"
        const val HTML = "https://sap.com"
        const val CAMPAIGN_ID = "campaignId"
    }

    private lateinit var webPushToInAppHandler: WebPushToInAppHandler
    private lateinit var mockInAppHandler: InAppHandlerApi
    private lateinit var mockInAppDownloader: InAppDownloaderApi

    @BeforeTest
    fun setup() {
        mockInAppDownloader = mock()
        mockInAppHandler = mock()
        everySuspend { mockInAppHandler.handle(CAMPAIGN_ID, any()) } returns Unit

        webPushToInAppHandler = WebPushToInAppHandler(mockInAppDownloader, mockInAppHandler)
    }

    @Test
    fun handle_shouldCallHandleOnInAppHandler_whenDownloadSucceeds_andHtml_isNotEmpty() = runTest {
        val testActionModel = InternalPushToInappActionModel(CAMPAIGN_ID, URL)
        everySuspend { mockInAppDownloader.download(URL) } returns HTML

        webPushToInAppHandler.handle(testActionModel)

        verifySuspend { mockInAppDownloader.download(URL) }
        verifySuspend { mockInAppHandler.handle(CAMPAIGN_ID, HTML) }
    }

    @Test
    fun handle_shouldNotCallHandleOnInAppHandler_whenHtml_isEmpty() = runTest {
        val testActionModel = InternalPushToInappActionModel(CAMPAIGN_ID, URL)
        everySuspend { mockInAppDownloader.download(URL) } returns ""

        webPushToInAppHandler.handle(testActionModel)

        verifySuspend { mockInAppDownloader.download(URL) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppHandler.handle(any(), any()) }
    }

    @Test
    fun handle_shouldNotCallHandleOnInAppHandler_whenHtml_isNull() = runTest {
        val testActionModel = InternalPushToInappActionModel(CAMPAIGN_ID, URL)
        everySuspend { mockInAppDownloader.download(URL) } returns null

        webPushToInAppHandler.handle(testActionModel)

        verifySuspend { mockInAppDownloader.download(URL) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppHandler.handle(any(), any()) }
    }
}