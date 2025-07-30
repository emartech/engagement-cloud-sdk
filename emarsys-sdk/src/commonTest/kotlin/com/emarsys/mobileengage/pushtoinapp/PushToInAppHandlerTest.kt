package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.core.log.Logger
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
            PushToInAppPayload(CAMPAIGN_ID, URL)
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
    private lateinit var mockLogger: Logger

    @BeforeTest
    fun setup() {
        mockInAppHandler = mock(MockMode.autofill)
        everySuspend { mockInAppHandler.handle(INAPP_MESSAGE) } returns Unit
        mockInAppDownLoader = mock()
        mockLogger = mock(MockMode.autofill)
        pushToInAppHandler = PushToInAppHandler(mockInAppDownLoader, mockInAppHandler, mockLogger)
    }

    @Test
    fun handle_shouldCall_downLoad_andPresent_ifDownload_succeeds() = runTest {
        everySuspend { mockInAppDownLoader.download(URL) } returns INAPP_MESSAGE

        pushToInAppHandler.handle(TEST_ACTION_MODEL)

        verifySuspend {
            mockInAppDownLoader.download(URL)
            mockInAppHandler.handle(INAPP_MESSAGE)
        }
    }

    @Test
    fun handle_shouldNotCall_downLoad_andPresent_ifDownload_succeeds_but_contentIsEmpty() =
        runTest {
            val inAppMessageWithEmptyContent = INAPP_MESSAGE.copy(content = "")
            everySuspend { mockInAppDownLoader.download(URL) } returns inAppMessageWithEmptyContent

            pushToInAppHandler.handle(TEST_ACTION_MODEL)

            verifySuspend {
                mockInAppDownLoader.download(URL)
            }

            verifySuspend(VerifyMode.exactly(0)) {
                mockInAppHandler.handle(any())
            }
        }

    @Test
    fun handle_shouldNotCall_Present_ifDownload_returnsNull() = runTest {
        everySuspend { mockInAppDownLoader.download(URL) } returns null

        pushToInAppHandler.handle(TEST_ACTION_MODEL)

        verifySuspend {
            mockInAppDownLoader.download(URL)
        }

        verifySuspend(VerifyMode.exactly(0)) {
            mockInAppHandler.handle(any())
        }
    }
}