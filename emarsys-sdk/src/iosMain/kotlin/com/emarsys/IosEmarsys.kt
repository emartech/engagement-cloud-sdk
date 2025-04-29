package com.emarsys

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.IosContactApi
import com.emarsys.api.extension.throwErrorFromResult
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.inapp.IosInAppApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.IosPushApi
import com.emarsys.api.tracking.IosTrackingApi
import com.emarsys.core.exceptions.SdkAlreadyDisabledException
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.di.SdkKoinIsolationContext.koin
import io.ktor.http.Url
import io.ktor.utils.io.CancellationException
import platform.Foundation.NSUserActivity
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("Emarsys")
object IosEmarsys {
    val contact: IosContactApi
        get() = koin.get<IosContactApi>()
    val push: IosPushApi
        get() = koin.get<IosPushApi>()
    val tracking: IosTrackingApi
        get() = koin.get<IosTrackingApi>()
    val inApp: IosInAppApi
        get() = koin.get<IosInAppApi>()
    val config: ConfigApi
        get() = Emarsys.config
    val geofence: GeofenceTrackerApi
        get() = Emarsys.geofence
    val predict: PredictApi
        get() = Emarsys.predict

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     */
    @Throws(CancellationException::class)
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
        Emarsys.enableTracking(config).throwErrorFromResult()
    }

    /**
     * Disables tracking.
     */
    @Throws(SdkAlreadyDisabledException::class, CancellationException::class)
    suspend fun disableTracking() {
        Emarsys.disableTracking().throwErrorFromResult()
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