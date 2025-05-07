package com.emarsys

import com.emarsys.api.config.IosConfigApi
import com.emarsys.api.contact.IosContactApi
import com.emarsys.api.deeplink.IosDeepLinkApi
import com.emarsys.api.geofence.IosGeofenceApi
import com.emarsys.api.inapp.IosInAppApi
import com.emarsys.api.predict.IosPredictApi
import com.emarsys.api.push.IosPushApi
import com.emarsys.api.tracking.IosTrackingApi
import com.emarsys.core.exceptions.SdkAlreadyDisabledException
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.qualifier.named
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("Emarsys")
object IosEmarsys {

    var events: ((SdkEvent) -> Unit)? = null

    val contact: IosContactApi
        get() = koin.get<IosContactApi>()
    val push: IosPushApi
        get() = koin.get<IosPushApi>()
    val tracking: IosTrackingApi
        get() = koin.get<IosTrackingApi>()
    val inApp: IosInAppApi
        get() = koin.get<IosInAppApi>()
    val config: IosConfigApi
        get() = koin.get<IosConfigApi>()
    val geofence: IosGeofenceApi
        get() = koin.get<IosGeofenceApi>()
    val predict: IosPredictApi
        get() = koin.get<IosPredictApi>()
    val deepLink: IosDeepLinkApi
        get() = koin.get<IosDeepLinkApi>()

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     */
    @Throws(CancellationException::class)
    suspend fun initialize() {
        Emarsys.initialize()
        koin.get<MutableSharedFlow<SdkEvent>>(named(EventFlowTypes.Public)).collect {
            events?.invoke(it)
        }
    }

    /**
     * Enables tracking with the provided configuration.
     *
     * @param config The SDK configuration to use for enabling tracking.
     */
    @Throws(SdkAlreadyEnabledException::class, CancellationException::class)
    suspend fun enableTracking(config: SdkConfig) {
        Emarsys.enableTracking(config).getOrThrow()
    }

    /**
     * Disables tracking.
     */
    @Throws(SdkAlreadyDisabledException::class, CancellationException::class)
    suspend fun disableTracking() {
        Emarsys.disableTracking().getOrThrow()
    }
}