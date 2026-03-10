package com.sap.ec.mobileengage.inapp

import com.sap.ec.SdkConstants
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.inapp.presentation.InAppPresentationMode
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import com.sap.ec.mobileengage.inapp.reporting.InAppLoadingMetric
import com.sap.ec.mobileengage.inapp.view.InAppViewApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import web.dom.document
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebInAppPresenterTests {

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockLogger: Logger
    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var presenter: WebInAppPresenter

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)
        sdkEventFlow = MutableSharedFlow()
        every { mockSdkEventDistributor.sdkEventFlow } returns sdkEventFlow

        presenter = WebInAppPresenter(
            sdkEventDistributor = mockSdkEventDistributor,
            sdkDispatcher = StandardTestDispatcher(),
            logger = mockLogger
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createMockInAppView(trackingInfo: String, type: InAppType = InAppType.OVERLAY): InAppViewApi {
        val mockView: InAppViewApi = mock(MockMode.autofill)
        every { mockView.inAppMessage } returns InAppMessage(
            dismissId = "testDismissId",
            type = type,
            trackingInfo = trackingInfo,
            content = "testContent"
        )
        return mockView
    }

    private fun createWebViewHolder(): WebWebViewHolder {
        val element = document.createElement("div")
        return WebWebViewHolder(
            webView = element,
            metrics = InAppLoadingMetric(0, 0)
        )
    }

    @Test
    fun present_shouldRegisterViewedEvent_afterPresentation() = runTest {
        val testTrackingInfo = """{"campaignId":"test123"}"""
        val mockView = createMockInAppView(testTrackingInfo)
        val webViewHolder = createWebViewHolder()

        val eventSlot = slot<SdkEvent>()
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(MockMode.autofill)

        presenter.present(mockView, webViewHolder, InAppPresentationMode.Overlay)

        val viewed = eventSlot.get().shouldBeInstanceOf<SdkEvent.Internal.InApp.Viewed>()
        viewed.trackingInfo shouldBe testTrackingInfo
        viewed.name shouldBe SdkConstants.INAPP_VIEWED_EVENT_NAME
    }

    @Test
    fun present_shouldNotRegisterViewedEvent_forInlineMessages() = runTest {
        val mockView = createMockInAppView("{}", InAppType.INLINE)
        val webViewHolder = createWebViewHolder()

        presenter.present(mockView, webViewHolder, InAppPresentationMode.Overlay)

        verifySuspend(VerifyMode.Companion.exactly(0)) {
            mockSdkEventDistributor.registerEvent(any())
        }
    }
}
