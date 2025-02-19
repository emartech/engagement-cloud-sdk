package com.emarsys.mobileengage.inapp

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.AfterTest

@OptIn(ExperimentalCoroutinesApi::class)
class InAppPresenterTests {

    private lateinit var mockCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog
    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>

    private lateinit var inAppPresenter: InAppPresenterApi


    @Before
    fun setup() {
        val mainDispatcher = StandardTestDispatcher()
        val sdkDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        mockCurrentActivityWatchdog = mockk<TransitionSafeCurrentActivityWatchdog>()
        sdkEventFlow = MutableSharedFlow(replay = 10)
        inAppPresenter = InAppPresenter(
            mockCurrentActivityWatchdog,
            mainDispatcher,
            sdkDispatcher,
            sdkEventFlow,
            mockk()
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun present_shouldAddInAppDialog_inFragmentTransaction_whenFragmentManagerIsAvailable() =
        runTest {
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
            inAppPresenter.present(mockView, mockWebViewHolder, InAppPresentationMode.Overlay)

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
            mockkStatic("com.emarsys.mobileengage.inapp.InAppPresenterKt")
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
}