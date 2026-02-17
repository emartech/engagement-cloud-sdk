package com.sap.ec.mobileengage.pushtoinapp

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.inapp.InAppMessage
import com.sap.ec.mobileengage.inapp.networking.download.InAppDownloaderApi
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.getIfPresent
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushToInAppHandlerTests {
    private companion object {
        const val URL = "https://sap.com"
        const val CONTENT = "<html></html>"
        const val ID = "testId"
        const val TRACKING_INFO = """{"trackingKey":"trackingValue"}"""
        val INAPP_MESSAGE = InAppMessage(
            dismissId = ID,
            type = InAppType.OVERLAY,
            trackingInfo = TRACKING_INFO,
            content = CONTENT
        )
    }

    private lateinit var pushToInAppHandler: PushToInAppHandler
    private lateinit var mockInAppDownLoader: InAppDownloaderApi
    private lateinit var mockLogger: Logger
    private lateinit var mockSdkEventManager: SdkEventManagerApi


    @BeforeTest
    fun setup() {
        mockInAppDownLoader = mock()
        mockLogger = mock(MockMode.autofill)
        mockSdkEventManager = mock(MockMode.autofill)
        pushToInAppHandler = PushToInAppHandler(
            mockInAppDownLoader,
            mockLogger,
            mockSdkEventManager
        )

    }

    @Test
    fun handle_shouldCall_downLoad_andEmitPresentEvent_ifDownload_succeeds() = runTest {
        val eventSlot = Capture.slot<SdkEvent.Internal.InApp.Present>()
        everySuspend { mockInAppDownLoader.download(URL) } returns INAPP_MESSAGE
        everySuspend { mockSdkEventManager.emitEvent(capture(eventSlot))} returns Unit

        pushToInAppHandler.handle(URL)

        verifySuspend {
            mockInAppDownLoader.download(URL)
            mockSdkEventManager.emitEvent(any())
        }
        eventSlot.getIfPresent()?.inAppMessage shouldBe INAPP_MESSAGE
    }

    @Test
    fun handle_shouldNot_emitPresentEvent_ifDownload_succeeds_but_contentIsEmpty() =
        runTest {
            val inAppMessageWithEmptyContent = INAPP_MESSAGE.copy(content = "")
            everySuspend { mockInAppDownLoader.download(URL) } returns inAppMessageWithEmptyContent

            pushToInAppHandler.handle(URL)

            verifySuspend {
                mockInAppDownLoader.download(URL)
            }
            verifySuspend(VerifyMode.exactly(0)) {
                mockSdkEventManager.emitEvent(any())
            }
        }

    @Test
    fun handle_shouldNot_emitPresentEvent_ifDownload_returnsNull() = runTest {
        everySuspend { mockInAppDownLoader.download(URL) } returns null

        pushToInAppHandler.handle(URL)

        verifySuspend {
            mockInAppDownLoader.download(URL)
        }
        verifySuspend(VerifyMode.exactly(0)) {
            mockSdkEventManager.emitEvent(any())
        }
    }
}