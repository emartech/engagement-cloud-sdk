package com.emarsys.mobileengage.inapp

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import kotlinx.coroutines.flow.SharedFlow

class InAppPresenter(
    private val currentActivityWatchdog: TransitionSafeCurrentActivityWatchdog,
    private val sdkEventFlow: SharedFlow<SdkEvent>
) : InAppPresenterApi {

    override suspend fun present(
        view: InAppViewApi,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation?
    ) {
        val inAppDialog = InAppDialog(view as InAppView)
        val currentActivity = currentActivityWatchdog.waitForActivity()
        currentActivity.fragmentManager()?.let {
            it.beginTransaction().run {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                add(android.R.id.content, inAppDialog, InAppDialog.TAG)
                addToBackStack(null)
                commit()
            }
        }

        // todo consume flow and dismiss
    }
}

fun Activity.fragmentManager(): FragmentManager? {
    var fragmentManager: FragmentManager? = null
    if (this is FragmentActivity) {
        fragmentManager = this.supportFragmentManager
    }
    return fragmentManager
}
