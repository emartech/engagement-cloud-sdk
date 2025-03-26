package com.emarsys

import android.app.Activity
import android.content.Intent
import com.emarsys.SdkConstants.EMS_DEEP_LINK_TRACKED_KEY
import com.emarsys.api.config.ConfigApi
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.log.Logger
import com.emarsys.di.SdkKoinIsolationContext.koin
import io.ktor.http.Url

object AndroidEmarsys {

    val push: PushApi
        get() = Emarsys.push
    val inApp: InAppApi
        get() = Emarsys.inApp
    val inbox: InboxApi
        get() = Emarsys.inbox
    val config: ConfigApi
        get() = Emarsys.config
    val geofence: GeofenceTrackerApi
        get() = Emarsys.geofence
    val predict: PredictApi
        get() = Emarsys.predict

    suspend fun initialize() {
        Emarsys.initialize()
    }

    suspend fun enableTracking(config: SdkConfig) {
        Emarsys.enableTracking(config)
    }

    suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        Emarsys.linkContact(contactFieldId, contactFieldValue)
    }

    suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        Emarsys.linkAuthenticatedContact(contactFieldId, openIdToken)
    }

    suspend fun unlinkContact() {
        Emarsys.unlinkContact()
    }

    suspend fun trackCustomEvent(event: String, attributes: Map<String, String>?) {
        Emarsys.trackCustomEvent(event, attributes)
    }

    suspend fun trackDeepLink(activity: Activity, intent: Intent) {
        val uri = intent.data
        val intentFromActivity: Intent? = activity.intent
        val isLinkTracked =
            intentFromActivity?.getBooleanExtra(EMS_DEEP_LINK_TRACKED_KEY, false) ?: false
        if (!isLinkTracked && uri != null) {
            Emarsys.deepLink.trackDeepLink(Url(uri.toString()))
            intentFromActivity?.putExtra(EMS_DEEP_LINK_TRACKED_KEY, true)
        } else {
            koin.get<Logger>().info(
                "AndroidEmarsys - trackDeepLink",
                "Cannot track deeplink with uri: $uri"
            )
        }
    }
}
