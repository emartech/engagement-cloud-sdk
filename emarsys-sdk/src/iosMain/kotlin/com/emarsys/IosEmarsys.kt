package com.emarsys

import com.emarsys.api.config.IosConfigApi
import com.emarsys.api.contact.IosContactApi
import com.emarsys.api.deeplink.IosDeepLinkApi
import com.emarsys.api.geofence.IosGeofenceApi
import com.emarsys.api.inapp.IosInAppApi
import com.emarsys.api.predict.IosPredictApi
import com.emarsys.api.push.IosPushApi
import com.emarsys.api.tracking.IosTrackingApi
import com.emarsys.config.SdkConfig
import com.emarsys.core.exceptions.SdkAlreadyDisabledException
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.di.CoroutineScopeTypes
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.event.SdkEvent
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import kotlin.experimental.ExperimentalObjCName

typealias EmarsysEventListener = (SdkEvent) -> Unit

@OptIn(ExperimentalObjCName::class)
@ObjCName("Emarsys")
object IosEmarsys {
    private var eventListeners: MutableList<EmarsysEventListener> = mutableListOf()

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
        koin.get<CoroutineScope>(named(CoroutineScopeTypes.Application)).launch(start = CoroutineStart.UNDISPATCHED) {
            koin.get<Flow<SdkEvent.External.Api>>(named(EventFlowTypes.Public)).collect {
                eventListeners.forEach { listener ->
                    listener.invoke(it)
                }
            }
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
     * Registers an event listener to receive SDK events.
     */
    fun registerEventListener(listener: EmarsysEventListener) {
        eventListeners.add(listener)
    }

    /**
     * Disables tracking.
     */
    @Throws(SdkAlreadyDisabledException::class, CancellationException::class)
    suspend fun disableTracking() {
        Emarsys.disableTracking().getOrThrow()
    }
}