package com.emarsys

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.deeplink.AndroidDeepLinkApi
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.tracking.TrackingApi

object AndroidEmarsys {

    val contact: ContactApi
        get() = Emarsys.contact
    val push: PushApi
        get() = Emarsys.push
    val tracking: TrackingApi
        get() = Emarsys.tracking
    val deepLink: AndroidDeepLinkApi
        get() = koin.get<AndroidDeepLinkApi>()
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
     * On Android it is being called automatically
     */
    suspend fun initialize() {
        Emarsys.initialize()
    }

    /**
     * Enables tracking with the provided configuration.
     *
     * @param config The SDK configuration to use for enabling tracking.
     * @throws SdkAlreadyEnabledException if tracking is already enabled.
     */
    suspend fun enableTracking(config: SdkConfig) {
        Emarsys.enableTracking(config)
    }

    /**
     * Disables tracking with the provided configuration.
     */
    suspend fun disableTracking() {
        Emarsys.disableTracking()
    }
}
