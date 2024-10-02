package com.emarsys.mobileengage.inapp

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.emarsys.core.message.MsgHubApi
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog

class InAppPresenter(
    private val currentActivityWatchdog: TransitionSafeCurrentActivityWatchdog,
    private val msgHub: MsgHubApi
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

        msgHub.subscribe("dismiss") {
            inAppDialog.dismiss()
        }
    }
}

fun Activity.fragmentManager(): FragmentManager? {
    var fragmentManager: FragmentManager? = null
    if (this is FragmentActivity) {
        fragmentManager = this.supportFragmentManager
    }
    return fragmentManager
}
