package com.sap.ec.api.deeplink

import android.app.Activity
import android.content.Intent
import com.sap.ec.SdkConstants.EMS_DEEP_LINK_TRACKED_KEY
import com.sap.ec.core.log.Logger
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

class AndroidDeepLink(
    private val sdkDispatcher: CoroutineDispatcher,
    private val deepLink: DeepLinkApi,
    private val sdkLogger: Logger
): AndroidDeepLinkApi {
    override fun track(
        activity: Activity,
        intent: Intent
    ): Boolean {
        var result = false;
        val uri = intent.data
        val intentFromActivity: Intent? = activity.intent
        val isLinkTracked =
            intentFromActivity?.getBooleanExtra(EMS_DEEP_LINK_TRACKED_KEY, false) ?: false
        if (!isLinkTracked && uri != null) {
            result = deepLink.track(Url(uri.toString())).getOrNull() ?: false
            intentFromActivity?.putExtra(EMS_DEEP_LINK_TRACKED_KEY, true)
        } else {
            CoroutineScope(sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
                sdkLogger.info(
                    "Cannot track deeplink with uri: $uri"
                )
            }

        }
        return result
    }
}