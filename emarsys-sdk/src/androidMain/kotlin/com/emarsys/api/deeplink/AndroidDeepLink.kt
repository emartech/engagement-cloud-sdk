package com.emarsys.api.deeplink

import android.app.Activity
import android.content.Intent
import com.emarsys.SdkConstants.EMS_DEEP_LINK_TRACKED_KEY
import com.emarsys.core.log.Logger
import io.ktor.http.Url

class AndroidDeepLink(private val deepLink: DeepLinkApi, private val sdkLogger: Logger): AndroidDeepLinkApi {
    override suspend fun trackDeepLink(
        activity: Activity,
        intent: Intent
    ) {
        val uri = intent.data
        val intentFromActivity: Intent? = activity.intent
        val isLinkTracked =
            intentFromActivity?.getBooleanExtra(EMS_DEEP_LINK_TRACKED_KEY, false) ?: false
        if (!isLinkTracked && uri != null) {
            deepLink.trackDeepLink(Url(uri.toString()))
            intentFromActivity?.putExtra(EMS_DEEP_LINK_TRACKED_KEY, true)
        } else {
            sdkLogger.info(
                "Cannot track deeplink with uri: $uri"
            )
        }
    }
}