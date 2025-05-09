package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.mobileengage.action.models.PresentablePushToInAppActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppType
import com.emarsys.mobileengage.inapp.PushToInAppPayload
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushToInAppHandlerTests {
    private companion object {
        const val URL = "https://sap.com"
        const val CAMPAIGN_ID = "campaignId"
        const val CONTENT = "<html></html>"
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
        val INAPP_MESSAGE = InAppMessage(
            dismissId = ID,
            type = InAppType.OVERLAY,
            trackingInfo = TRACKING_INFO,
            content = CONTENT
        )
    }

    private lateinit var pushToInAppHandler: PushToInAppHandler
    private lateinit var mockInAppDownLoader: InAppDownloaderApi
    private lateinit var mockInAppHandler: InAppHandlerApi

    @BeforeTest
    fun setup() {
        mockInAppHandler = mock(MockMode.autofill)
        everySuspend { mockInAppHandler.handle(INAPP_MESSAGE) } returns Unit
        mockInAppDownLoader = mock()
        pushToInAppHandler = PushToInAppHandler(mockInAppDownLoader, mockInAppHandler)
    }

    @Test
    fun handle_shouldCall_downLoad_andPresent_ifDownload_succeeds() = runTest {
        everySuspend { mockInAppDownLoader.download(URL) } returns CONTENT

        pushToInAppHandler.handle(TEST_ACTION_MODEL)

        verifySuspend {
            mockInAppDownLoader.download(URL)
            mockInAppHandler.handle(INAPP_MESSAGE)
        }
    }

    @Test
    fun handle_shouldNotCall_downLoad_andPresent_ifDownload_succeeds_but_contentIsEmpty() =
        runTest {
            everySuspend { mockInAppDownLoader.download(URL) } returns ""

            pushToInAppHandler.handle(TEST_ACTION_MODEL)

            verifySuspend {
                mockInAppDownLoader.download(URL)
            }

            verifySuspend(VerifyMode.exactly(0)) {
                mockInAppHandler.handle(any())
            }
        }

    @Test
    fun handle_shouldNotCall_downLoad_andPresent_ifDownload_fails() = runTest {
        everySuspend { mockInAppDownLoader.download(URL) } returns null

        pushToInAppHandler.handle(TEST_ACTION_MODEL)

        verifySuspend {
            mockInAppDownLoader.download(URL)
        }

        verifySuspend(VerifyMode.exactly(0)) {
            mockInAppHandler.handle(any())
        }
    }

    @Test
    fun handle_shouldNotCall_downLoad_andPresent_ifTrackingInfo_isNull() = runTest {
        everySuspend { mockInAppDownLoader.download(URL) } returns CONTENT

        pushToInAppHandler.handle(TEST_ACTION_MODEL.copy(trackingInfo = null))

        verifySuspend {
            mockInAppDownLoader.download(URL)
        }

        verifySuspend(VerifyMode.exactly(0)) {
            mockInAppHandler.handle(any())
        }
    }
}