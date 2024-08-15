package com.emarsys.mobileengage.inapp

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class InAppPresenterTests {

    private lateinit var mockCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog

    private lateinit var inAppPresenter: InAppPresenterApi


    @Before
    fun setup() {
        mockCurrentActivityWatchdog = mockk<TransitionSafeCurrentActivityWatchdog>()
        inAppPresenter = InAppPresenter(mockCurrentActivityWatchdog)
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
            every { mockCurrentActivityWatchdog.currentActivity } returns mockActivity
            val mockView = mockk<InAppView>()

            inAppPresenter.present(mockView, InAppPresentationMode.Overlay)

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
            every { mockCurrentActivityWatchdog.currentActivity } returns mockActivity
            val mockView = mockk<InAppView>()

            inAppPresenter.present(mockView, InAppPresentationMode.Overlay)

            verify { mockActivity.fragmentManager() }
            verify { mockActivity wasNot Called }
            unmockkAll()
        }
}