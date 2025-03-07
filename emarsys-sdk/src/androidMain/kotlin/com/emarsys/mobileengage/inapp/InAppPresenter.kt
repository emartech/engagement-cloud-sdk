package com.emarsys.mobileengage.inapp

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InAppPresenter(
    private val currentActivityWatchdog: TransitionSafeCurrentActivityWatchdog,
    private val mainDispatcher: CoroutineDispatcher,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkEventFlow: SharedFlow<SdkEvent>,
    private val logger: Logger
) : InAppPresenterApi {

    override suspend fun present(
        inAppView: InAppViewApi,
        webViewHolder: WebViewHolder,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation?
    ) {
        val inAppDialog = InAppDialog(inAppView as InAppView)
        val currentActivity = currentActivityWatchdog.waitForActivity()
        currentActivity.fragmentManager()?.let {
            it.beginTransaction().run {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                add(android.R.id.content, inAppDialog, InAppDialog.TAG)
                addToBackStack(null)
                commit()
            }
        }
        CoroutineScope(sdkDispatcher).launch {
            sdkEventFlow.first { it is SdkEvent.Internal.Sdk.Dismiss && it.id == inAppView.inAppMessage.campaignId }
            withContext(mainDispatcher) {
                logger.debug("InAppPresenter", "dismiss inapp dialog")
                inAppDialog.dismiss()
            }
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
