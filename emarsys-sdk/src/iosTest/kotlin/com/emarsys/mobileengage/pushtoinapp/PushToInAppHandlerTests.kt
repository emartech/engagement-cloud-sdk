package com.emarsys.mobileengage.pushtoinapp

import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
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
        const val HTML = "testHtml"
        val testActionModel = InternalPushToInappActionModel(CAMPAIGN_ID, URL)
    }

    private lateinit var pushToInAppHandler: PushToInAppHandler
    private lateinit var mockInAppDownLoader: InAppDownloaderApi
    private lateinit var mockInAppHandler: InAppHandlerApi

    @BeforeTest
    fun setup() {
        mockInAppHandler = mock()
        everySuspend { mockInAppHandler.handle(HTML) } returns Unit
        mockInAppDownLoader = mock()
        pushToInAppHandler = PushToInAppHandler(mockInAppDownLoader, mockInAppHandler)
    }

    @Test
    fun handle_shouldCall_downLoad_andPresent_ifDownload_succeeds() = runTest {
        everySuspend { mockInAppDownLoader.download(URL) } returns HTML

        pushToInAppHandler.handle(testActionModel)

        verifySuspend {
            mockInAppDownLoader.download(URL)
            mockInAppHandler.handle(HTML)
        }
    }

    @Test
    fun handle_shouldNotCall_downLoad_andPresent_ifDownload_fails() = runTest {
        everySuspend { mockInAppDownLoader.download(URL) } returns null

        pushToInAppHandler.handle(testActionModel)

        verifySuspend {
            mockInAppDownLoader.download(URL)
        }

        verifySuspend(VerifyMode.exactly(0)) {
            mockInAppHandler.handle(HTML)
        }
    }
}