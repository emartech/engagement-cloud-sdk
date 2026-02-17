package com.sap.ec.mobileengage.inapp

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.inapp.presentation.InAppPresentationAnimation
import com.sap.ec.mobileengage.inapp.presentation.InAppPresentationMode
import com.sap.ec.mobileengage.inapp.presentation.InAppPresenterApi
import com.sap.ec.mobileengage.inapp.provider.InAppDialogProviderApi
import com.sap.ec.mobileengage.inapp.reporting.InAppLoadingMetric
import com.sap.ec.mobileengage.inapp.view.InAppDialog
import com.sap.ec.mobileengage.inapp.view.InAppView
import com.sap.ec.mobileengage.inapp.view.InAppViewApi
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder
import com.sap.ec.watchdog.activity.TransitionSafeCurrentActivityWatchdog
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
    private val inAppDialogProvider: InAppDialogProviderApi,
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
        val inAppDialog = inAppDialogProvider.provide().apply {
            setInAppView(inAppView as InAppView)
        }
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
