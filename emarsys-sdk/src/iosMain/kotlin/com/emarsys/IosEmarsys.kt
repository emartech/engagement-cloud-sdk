package com.emarsys

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.core.exceptions.SdkAlreadyDisabledException
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.mobileengage.push.IosPushApi
import io.ktor.http.Url
import io.ktor.utils.io.CancellationException
import platform.Foundation.NSUserActivity
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("Emarsys")
object IosEmarsys {
    val contact: ContactApi
        get() = Emarsys.contact
    val push: IosPushApi
        get() = Emarsys.push as IosPushApi
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

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     */
    suspend fun initialize() {
        Emarsys.initialize()
    }

    /**
     * Enables tracking with the provided configuration.
     *
     * @param config The SDK configuration to use for enabling tracking.
     */
    @Throws(SdkAlreadyEnabledException::class, CancellationException::class)
    suspend fun enableTracking(config: SdkConfig) {
        val result = Emarsys.enableTracking(config)
        if (result.isFailure) {
            result.exceptionOrNull()?.let {
                throw it
            }
        }
    }

    /**
     * Disables tracking.
     *
     */
    @Throws(SdkAlreadyDisabledException::class, CancellationException::class)
    suspend fun disableTracking() {
        val result = Emarsys.disableTracking()
        if (result.isFailure) {
            result.exceptionOrNull()?.let {
                throw it
            }
        }
    }

    /**
     * Tracks a custom event with the specified name and optional attributes. These custom events can be used to trigger In-App campaigns or any automation configured at Emarsys.
     *
     * @param event The name of the custom event.
     * @param attributes Optional attributes for the event.
     */
    suspend fun trackCustomEvent(event: String, attributes: Map<String, String>?) {
        Emarsys.trackCustomEvent(event, attributes)
    }

    /**
     * Tracks a deep link using the provided user activity.
     *
     * @param userActivity The user activity containing the deep link information.
     */
    suspend fun trackDeepLink(userActivity: NSUserActivity) {
        userActivity.webpageURL?.absoluteString()?.let {
            Emarsys.deepLink.trackDeepLink(Url(it))
        }
    }
}