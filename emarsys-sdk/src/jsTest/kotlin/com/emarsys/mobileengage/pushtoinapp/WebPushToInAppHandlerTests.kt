package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.mobileengage.action.models.PresentablePushToInAppActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppType
import com.emarsys.mobileengage.inapp.PushToInAppPayload
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
        const val ID = "testId"
        const val TITLE = "testTitle"
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
        const val TRACKING_INFO = """{"trackingKey":"trackingValue"}"""
        val TEST_ACTION_MODEL = PresentablePushToInAppActionModel(
            ID,
            REPORTING,
            TITLE,
            PushToInAppPayload(CAMPAIGN_ID, URL),
            TRACKING_INFO
        )
        val INAPP_MESSAGE =
            InAppMessage(
                dismissId = ID,
                type = InAppType.OVERLAY,
                trackingInfo = TRACKING_INFO,
                content = HTML
            )
    }

    private lateinit var webPushToInAppHandler: WebPushToInAppHandler
    private lateinit var mockInAppHandler: InAppHandlerApi
    private lateinit var mockInAppDownloader: InAppDownloaderApi

    @BeforeTest
    fun setup() {
        mockInAppDownloader = mock()
        mockInAppHandler = mock()
        everySuspend { mockInAppHandler.handle(INAPP_MESSAGE) } returns Unit

        webPushToInAppHandler =
            WebPushToInAppHandler(mockInAppDownloader, mockInAppHandler)
    }

    @Test
    fun handle_shouldCallHandleOnInAppHandler_whenDownloadSucceeds_andHtml_isNotEmpty() = runTest {
        everySuspend { mockInAppDownloader.download(URL) } returns HTML

        webPushToInAppHandler.handle(TEST_ACTION_MODEL)

        verifySuspend { mockInAppDownloader.download(URL) }
        verifySuspend { mockInAppHandler.handle(INAPP_MESSAGE) }
    }

    @Test
    fun handle_shouldNotCallHandleOnInAppHandler_whenHtml_isEmpty() = runTest {
        everySuspend { mockInAppDownloader.download(URL) } returns ""

        webPushToInAppHandler.handle(TEST_ACTION_MODEL)

        verifySuspend { mockInAppDownloader.download(URL) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppHandler.handle(any()) }
    }

    @Test
    fun handle_shouldNotCallHandleOnInAppHandler_whenHtml_isNull() = runTest {
        everySuspend { mockInAppDownloader.download(URL) } returns null

        webPushToInAppHandler.handle(TEST_ACTION_MODEL)

        verifySuspend { mockInAppDownloader.download(URL) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppHandler.handle(any()) }
    }

    @Test
    fun handle_shouldNotCallHandleOnInAppHandler_whenTrackingInfo_isNull() = runTest {
        everySuspend { mockInAppDownloader.download(URL) } returns null

        webPushToInAppHandler.handle(TEST_ACTION_MODEL.copy(trackingInfo = null))

        verifySuspend { mockInAppDownloader.download(URL) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppHandler.handle(any()) }
    }
}