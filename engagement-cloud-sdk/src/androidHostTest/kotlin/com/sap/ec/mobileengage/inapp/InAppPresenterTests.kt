package com.sap.ec.mobileengage.inapp

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.sap.ec.SdkConstants
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.inapp.presentation.InAppPresentationMode
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import com.sap.ec.mobileengage.inapp.provider.InAppDialogProviderApi
import com.sap.ec.mobileengage.inapp.reporting.InAppLoadingMetric
import com.sap.ec.mobileengage.inapp.view.InAppDialog
import com.sap.ec.mobileengage.inapp.view.InAppView
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder
import com.sap.ec.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Before
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class InAppPresenterTests {

    private lateinit var mockCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog
    private lateinit var mockInAppDialog: InAppDialog
    private lateinit var mockInAppDialogProvider: InAppDialogProviderApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var mockLogger: Logger
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mainDispatcher: CoroutineDispatcher
    private lateinit var mockFragmentTransaction: FragmentTransaction
    private lateinit var mockFragmentManager: FragmentManager
    private lateinit var mockActivity: FragmentActivity
    private lateinit var mockView: InAppView
    private lateinit var mockWebViewHolder: WebViewHolder

    @Before
    fun setup() {
        mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        mockCurrentActivityWatchdog = mockk()
        mockInAppDialog = mockk(relaxed = true)
        mockInAppDialogProvider = mockk(relaxed = true)
        every { mockInAppDialogProvider.provide() } returns mockInAppDialog
        mockSdkEventDistributor = mockk(relaxed = true)
        sdkEventFlow = MutableSharedFlow()
        mockLogger = mockk(relaxed = true)
        mockTimestampProvider = mockk(relaxed = true)
        every { mockSdkEventDistributor.sdkEventFlow } returns sdkEventFlow

        mockFragmentTransaction = mockk(relaxed = true)
        mockFragmentManager = mockk(relaxed = true) {
            every { beginTransaction() } returns mockFragmentTransaction
        }
        mockActivity = mockk {
            every { supportFragmentManager } returns mockFragmentManager
        }
        mockView = mockk()
        mockWebViewHolder = mockk()
        coEvery { mockView.load(any()) } returns mockWebViewHolder
    }

    private fun createPresenterWithScope(scope: CoroutineScope): InAppPresenter {
        return InAppPresenter(
            mockInAppDialogProvider,
            mockCurrentActivityWatchdog,
            mainDispatcher,
            mockSdkEventDistributor,
            timestampProvider = mockTimestampProvider,
            logger = mockLogger,
            applicationScope = scope,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun present_shouldAddInAppDialog_inFragmentTransaction_whenFragmentManagerIsAvailable() =
        runTest {
            val testId = "testId"
            coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns mockActivity
            every { mockView.inAppMessage } returns mockk(relaxed = true) {
                every { trackingInfo } returns testId
            }

            val inAppPresenter = createPresenterWithScope(backgroundScope)
            inAppPresenter.present(mockView, mockWebViewHolder, InAppPresentationMode.Overlay)
            advanceUntilIdle()

            verify { mockInAppDialog.setInAppView(mockView) }
            verify { mockFragmentManager.beginTransaction() }
            verify { mockFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN) }
            verify {
                mockFragmentTransaction.add(
                    android.R.id.content,
                    any<InAppDialog>(),
                    InAppDialog.TAG
                )
            }
            verify { mockFragmentTransaction.addToBackStack(null) }
            verify { mockFragmentTransaction.commit() }
        }

    @Test
    fun present_shouldRegisterViewedEvent_afterFragmentCommit() =
        runTest {
            val testTrackingInfo = """{"campaignId":"test123"}"""
            coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns mockActivity
            every { mockView.inAppMessage } returns InAppMessage(
                dismissId = "testDismissId",
                trackingInfo = testTrackingInfo,
                content = "testContent"
            )

            val eventSlot = slot<SdkEvent>()
            coEvery { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockk(relaxed = true)

            val inAppPresenter = createPresenterWithScope(backgroundScope)
            inAppPresenter.present(mockView, mockWebViewHolder, InAppPresentationMode.Overlay)
            advanceUntilIdle()

            val viewed = eventSlot.captured.shouldBeInstanceOf<SdkEvent.Internal.InApp.Viewed>()
            viewed.trackingInfo shouldBe testTrackingInfo
            viewed.name shouldBe SdkConstants.INAPP_VIEWED_EVENT_NAME
        }

    @Test
    fun present_shouldNotAddInAppDialog_whenActivityIsNotFragmentActivity() =
        runTest {
            mockkStatic("com.sap.ec.mobileengage.inapp.InAppPresenterKt")
            val nonFragmentActivity = mockk<Activity>()
            coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns nonFragmentActivity
            every { mockView.inAppMessage } returns InAppMessage(
                dismissId = "testDismissId",
                trackingInfo = "testTracking",
                content = "testContent"
            )

            val inAppPresenter = createPresenterWithScope(backgroundScope)
            inAppPresenter.present(mockView, mockWebViewHolder, InAppPresentationMode.Overlay)

            verify { nonFragmentActivity.fragmentManager() }
            verify { nonFragmentActivity wasNot Called }
            unmockkAll()
        }

    @Test
    fun present_shouldLogMetrics() = runTest {
        every { mockTimestampProvider.provide().toEpochMilliseconds() } returnsMany listOf(10L, 20L)
        val testTrackingInfo = "testId"
        val testDismissId = "testDismissId"
        coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns mockActivity
        every { mockWebViewHolder.metrics } returns InAppLoadingMetric(10, 20)

        val inAppMessage = InAppMessage(
            dismissId = testDismissId,
            type = InAppType.OVERLAY,
            trackingInfo = testTrackingInfo,
            content = "<html></html>",
        )
        every { mockView.inAppMessage } returns inAppMessage

        val inAppPresenter = createPresenterWithScope(TestScope(SupervisorJob()))
        inAppPresenter.present(mockView, mockWebViewHolder, InAppPresentationMode.Overlay)
        advanceUntilIdle()

        sdkEventFlow.emit(SdkEvent.Internal.Sdk.Dismiss(id = testDismissId))
        advanceUntilIdle()

        coVerify {
            mockLogger.metric("InAppMetric", buildJsonObject {
                put("trackingInfo", testTrackingInfo)
                put("loadingTimeStart", 10)
                put("loadingTimeEnd", (20))
                put("loadingTimeDuration", (10))
                put("onScreenTimeStart", (10))
                put("onScreenTimeEnd", (20))
                put("onScreenTimeDuration", (10))
            })
        }
    }

    @Test
    fun present_shouldDismissActiveDialog_whenActiveDialogFoundAfterDismissEvent() = runTest {
        every { mockTimestampProvider.provide().toEpochMilliseconds() } returnsMany listOf(10L, 20L)
        val testDismissId = "testDismissId"
        coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns mockActivity

        val mockActiveDialog = mockk<InAppDialog>(relaxed = true)
        every { mockFragmentManager.findFragmentByTag(InAppDialog.TAG) } returns mockActiveDialog
        coEvery { mockCurrentActivityWatchdog.currentActivity() } returns mockActivity

        every { mockWebViewHolder.metrics } returns InAppLoadingMetric(10, 20)
        val inAppMessage = InAppMessage(
            dismissId = testDismissId,
            type = InAppType.OVERLAY,
            trackingInfo = "testTrackingInfo",
            content = "<html></html>",
        )
        every { mockView.inAppMessage } returns inAppMessage

        val inAppPresenter = createPresenterWithScope(TestScope(SupervisorJob()))
        inAppPresenter.present(mockView, mockWebViewHolder, InAppPresentationMode.Overlay)
        advanceUntilIdle()

        sdkEventFlow.emit(SdkEvent.Internal.Sdk.Dismiss(id = testDismissId))
        advanceUntilIdle()

        verify { mockActiveDialog.dismiss() }
        verify(exactly = 0) { mockInAppDialog.dismiss() }
    }

    @Test
    fun present_shouldDismissOriginalDialog_whenNoActiveDialogFound() = runTest {
        every { mockTimestampProvider.provide().toEpochMilliseconds() } returnsMany listOf(10L, 20L)
        val testDismissId = "testDismissId"
        every { mockFragmentManager.findFragmentByTag(InAppDialog.TAG) } returns null
        coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns mockActivity
        coEvery { mockCurrentActivityWatchdog.currentActivity() } returns mockActivity

        every { mockWebViewHolder.metrics } returns InAppLoadingMetric(10, 20)
        val inAppMessage = InAppMessage(
            dismissId = testDismissId,
            type = InAppType.OVERLAY,
            trackingInfo = "testTrackingInfo",
            content = "<html></html>",
        )
        every { mockView.inAppMessage } returns inAppMessage

        val inAppPresenter = createPresenterWithScope(TestScope(SupervisorJob()))
        inAppPresenter.present(mockView, mockWebViewHolder, InAppPresentationMode.Overlay)
        advanceUntilIdle()

        sdkEventFlow.emit(SdkEvent.Internal.Sdk.Dismiss(id = testDismissId))
        advanceUntilIdle()

        verify { mockInAppDialog.dismiss() }
    }

    @Test
    fun present_shouldDismissOriginalDialog_whenCurrentActivityIsNull() = runTest {
        every { mockTimestampProvider.provide().toEpochMilliseconds() } returnsMany listOf(10L, 20L)
        val testDismissId = "testDismissId"
        coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns mockActivity
        coEvery { mockCurrentActivityWatchdog.currentActivity() } returns null

        every { mockWebViewHolder.metrics } returns InAppLoadingMetric(10, 20)
        val inAppMessage = InAppMessage(
            dismissId = testDismissId,
            type = InAppType.OVERLAY,
            trackingInfo = "testTrackingInfo",
            content = "<html></html>",
        )
        every { mockView.inAppMessage } returns inAppMessage

        val inAppPresenter = createPresenterWithScope(TestScope(SupervisorJob()))
        inAppPresenter.present(mockView, mockWebViewHolder, InAppPresentationMode.Overlay)
        advanceUntilIdle()

        sdkEventFlow.emit(SdkEvent.Internal.Sdk.Dismiss(id = testDismissId))
        advanceUntilIdle()

        verify { mockInAppDialog.dismiss() }
    }
}