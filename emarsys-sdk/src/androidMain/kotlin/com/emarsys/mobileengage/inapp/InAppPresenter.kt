package com.emarsys.mobileengage.inapp

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.event.SdkEvent
import com.emarsys.watchdog.activity.TransitionSafeCurrentActivityWatchdog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.ExperimentalTime

internal class InAppPresenter(
    private val currentActivityWatchdog: TransitionSafeCurrentActivityWatchdog,
    private val mainDispatcher: CoroutineDispatcher,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val timestampProvider: InstantProvider,
    private val logger: Logger,
    private val applicationScope: CoroutineScope
) : InAppPresenterApi {
    override suspend fun trackMetric(
        trackingInfo: String,
        loadingMetric: InAppLoadingMetric,
        onScreenTimeStart: Long,
        onScreenTimeEnd: Long
    ) {
        logger.metric(
            message = "InAppMetric",
            data = buildJsonObject {
                put("trackingInfo", JsonPrimitive(trackingInfo))
                put(
                    "loadingTimeStart",
                    JsonPrimitive(loadingMetric.loadingStarted)
                )
                put(
                    "loadingTimeEnd",
                    JsonPrimitive(loadingMetric.loadingEnded)
                )
                put(
                    "loadingTimeDuration",
                    JsonPrimitive(loadingMetric.loadingEnded - loadingMetric.loadingStarted)
                )
                put("onScreenTimeStart", JsonPrimitive(onScreenTimeStart))
                put("onScreenTimeEnd", JsonPrimitive(onScreenTimeEnd))
                put(
                    "onScreenTimeDuration",
                    JsonPrimitive(onScreenTimeEnd - onScreenTimeStart)
                )
            }
        )
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun present(
        inAppView: InAppViewApi,
        webViewHolder: WebViewHolder,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation?
    ) {
        val inAppDialog = InAppDialog(inAppView as InAppView)
        val currentActivity = currentActivityWatchdog.waitForActivity()
        val onScreenTimeStart = timestampProvider.provide().toEpochMilliseconds()
        currentActivity.fragmentManager()?.let {
            it.beginTransaction().run {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                add(android.R.id.content, inAppDialog, InAppDialog.TAG)
                addToBackStack(null)
                commit()
            }
            applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
                sdkEventDistributor.sdkEventFlow.first { sdkEvent ->
                    sdkEvent is SdkEvent.Internal.Sdk.Dismiss && sdkEvent.id == inAppView.inAppMessage.dismissId
                }
                val onScreenTimeEnd = timestampProvider.provide().toEpochMilliseconds()
                logger.debug("dismiss in-app dialog")
                trackMetric(
                    inAppView.inAppMessage.trackingInfo,
                    webViewHolder.metrics,
                    onScreenTimeStart,
                    onScreenTimeEnd
                )
                withContext(mainDispatcher) {
                    inAppDialog.dismiss()
                }
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
