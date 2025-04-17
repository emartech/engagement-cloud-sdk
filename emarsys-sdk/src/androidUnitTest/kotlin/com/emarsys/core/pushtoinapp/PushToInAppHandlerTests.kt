package com.emarsys.core.pushtoinapp

import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.mobileengage.action.models.PresentablePushToInAppActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.PushToInAppPayload
import com.emarsys.mobileengage.pushtoinapp.PushToInAppHandler
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PushToInAppHandlerTests {
    private companion object {
        const val CAMPAIGN_ID = "testCampaignId"
        const val REPORTING = """{"key":"value"}"""
        const val ID = "testId"
        const val TITLE = "testTitle"
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
            mockInAppHandler,
            SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock())
        )
    }

    @Test
    fun handle_shouldDownloadHtml_andCallHandle_ifDownloadSucceeds() = runTest {
        val payload = PushToInAppPayload(CAMPAIGN_ID, INAPP_URL)
        val testInappAction =
            PresentablePushToInAppActionModel(
                id = ID,
                reporting = REPORTING,
                title = TITLE,
                payload = payload
            )

        coEvery { mockInAppDownloader.download(INAPP_URL) } returns HTML

        pushToInAppHandler.handle(testInappAction)

        coVerify { mockInAppDownloader.download(INAPP_URL) }
        coVerify { mockInAppHandler.handle(CAMPAIGN_ID, HTML) }
    }

    @Test
    fun handle_shouldDownloadHtml_andNotCallHandle_ifDownloadFails() = runTest {
        val payload = PushToInAppPayload(CAMPAIGN_ID, INAPP_URL)
        val testInappAction =
            PresentablePushToInAppActionModel(
                id = ID,
                reporting = REPORTING,
                title = TITLE,
                payload = payload
            )

        coEvery { mockInAppDownloader.download(INAPP_URL) } returns null

        pushToInAppHandler.handle(testInappAction)

        coVerify { mockInAppDownloader.download(INAPP_URL) }
        coVerify(exactly = 0) { mockInAppHandler.handle(any(), any()) }
    }
}