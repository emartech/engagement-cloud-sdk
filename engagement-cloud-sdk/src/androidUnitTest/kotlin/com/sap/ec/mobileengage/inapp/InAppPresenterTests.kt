package com.sap.ec.mobileengage.inapp

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.inapp.presentation.InAppPresentationMode
import com.sap.ec.mobileengage.inapp.presentation.InAppPresenterApi
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import com.sap.ec.mobileengage.inapp.provider.InAppDialogProviderApi
import com.sap.ec.mobileengage.inapp.reporting.InAppLoadingMetric
import com.sap.ec.mobileengage.inapp.view.InAppDialog
import com.sap.ec.mobileengage.inapp.view.InAppView
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder
import com.sap.ec.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
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
    private lateinit var inAppPresenter: InAppPresenterApi
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mainDispatcher: CoroutineDispatcher

    @Before
    fun setup() {
        mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        mockCurrentActivityWatchdog = mockk<TransitionSafeCurrentActivityWatchdog>()
        mockInAppDialog = mockk(relaxed = true)
        mockInAppDialogProvider = mockk(relaxed = true)
        every { mockInAppDialogProvider.provide() } returns mockInAppDialog
        mockSdkEventDistributor = mockk(relaxed = true)
        sdkEventFlow = MutableSharedFlow()
        mockLogger = mockk(relaxed = true)
        mockTimestampProvider = mockk(relaxed = true)
        every { mockSdkEventDistributor.sdkEventFlow } returns sdkEventFlow
        inAppPresenter = InAppPresenter(
            mockInAppDialogProvider,
            mockCurrentActivityWatchdog,
            mainDispatcher,
            mockSdkEventDistributor,
            timestampProvider = mockTimestampProvider,
            logger = mockLogger,
            applicationScope = mockk(),
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
            val mockFragmentTransaction = mockk<FragmentTransaction>(relaxed = true)
            val mockFragmentManager = mockk<FragmentManager>(relaxed = true) {
                every { beginTransaction() } returns mockFragmentTransaction
            }
            val mockActivity = mockk<FragmentActivity> {
                every { supportFragmentManager } returns mockFragmentManager
            }
            coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns mockActivity
            val mockView = mockk<InAppView>()
            val mockWebViewHolder = mockk<WebViewHolder>()
            coEvery { mockView.load(any()) } returns mockWebViewHolder
            every { mockView.inAppMessage } returns
                    mockk<InAppMessage>(relaxed = true) {
                        every { trackingInfo } returns testId
                    }

            val inAppPresenter = InAppPresenter(
                mockInAppDialogProvider,
                mockCurrentActivityWatchdog,
                mainDispatcher,
                mockSdkEventDistributor,
                timestampProvider = mockTimestampProvider,
                logger = mockLogger,
                applicationScope = backgroundScope,
            )

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
    fun present_shouldNotAddInAppDialog_whenActivityIsNotFragmentActivity() =
        runTest {
            mockkStatic("com.sap.ec.mobileengage.inapp.InAppPresenterKt")
            val mockActivity = mockk<Activity>()
            coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns mockActivity
            val mockView = mockk<InAppView>()
            val mockWebViewHolder = mockk<WebViewHolder>()
            coEvery { mockView.load(any()) } returns mockWebViewHolder
            inAppPresenter.present(mockView, mockWebViewHolder, InAppPresentationMode.Overlay)

            verify { mockActivity.fragmentManager() }
            verify { mockActivity wasNot Called }
            unmockkAll()
        }

    @Test
    fun present_shouldLogMetrics() = runTest {
        every { mockTimestampProvider.provide().toEpochMilliseconds() } returnsMany listOf(10L, 20L)
        val testTrackingInfo = "testId"
        val testDismissId = "testDismissId"
        val mockFragmentTransaction = mockk<FragmentTransaction>(relaxed = true)
        val mockFragmentManager = mockk<FragmentManager>(relaxed = true) {
            every { beginTransaction() } returns mockFragmentTransaction
        }
        val mockActivity = mockk<FragmentActivity> {
            every { supportFragmentManager } returns mockFragmentManager
        }
        coEvery { mockCurrentActivityWatchdog.waitForActivity() } returns mockActivity
        val mockView = mockk<InAppView>()
        val mockWebViewHolder = mockk<WebViewHolder>()
        coEvery { mockView.load(any()) } returns mockWebViewHolder
        every { mockWebViewHolder.metrics } returns InAppLoadingMetric(10, 20)

        val inAppMessage = InAppMessage(
            dismissId = testDismissId,
            type = InAppType.OVERLAY,
            trackingInfo = testTrackingInfo,
            content = "<html></html>",
        )
        every { mockView.inAppMessage } returns inAppMessage
        val inAppPresenter = InAppPresenter(
            mockInAppDialogProvider,
            mockCurrentActivityWatchdog,
            mainDispatcher,
            mockSdkEventDistributor,
            timestampProvider = mockTimestampProvider,
            logger = mockLogger,
            applicationScope = TestScope(SupervisorJob()),
        )

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
}