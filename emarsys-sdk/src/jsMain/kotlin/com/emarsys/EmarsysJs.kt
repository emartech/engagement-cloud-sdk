package com.emarsys

import com.emarsys.api.config.JSConfigApi
import com.emarsys.api.contact.JSContactApi
import com.emarsys.api.deeplink.JSDeepLinkApi
import com.emarsys.api.geofence.JSGeofenceApi
import com.emarsys.api.inapp.JSInAppApi
import com.emarsys.api.inbox.JSInboxApi
import com.emarsys.api.push.JSPushApi
import com.emarsys.api.tracking.JSTrackingApi
import com.emarsys.di.CoroutineScopeTypes
import com.emarsys.di.SdkKoinIsolationContext.koin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import org.koin.core.qualifier.named
import kotlin.js.Promise

suspend fun main() {
    EmarsysJs.init().await()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("EmarsysJs")
object EmarsysJs {

    private lateinit var applicationScope: CoroutineScope
    lateinit var config: JSConfigApi
    lateinit var contact: JSContactApi
    lateinit var tracking: JSTrackingApi
    lateinit var push: JSPushApi
    lateinit var deepLink: JSDeepLinkApi
    lateinit var geofence: JSGeofenceApi
    lateinit var inbox: JSInboxApi
    lateinit var inApp: JSInAppApi

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     *
     * @return A promise that resolves when the initialization is complete.
     */
    fun init(): Promise<Unit> {
        return CoroutineScope(SupervisorJob()).promise {
            Emarsys.initialize()
            applicationScope = koin.get<CoroutineScope>(named(CoroutineScopeTypes.Application))
            config = koin.get<JSConfigApi>()
            contact = koin.get<JSContactApi>()
            tracking = koin.get<JSTrackingApi>()
            push = koin.get<JSPushApi>()
            deepLink = koin.get<JSDeepLinkApi>()
            geofence = koin.get<JSGeofenceApi>()
            inbox = koin.get<JSInboxApi>()
            inApp = koin.get<JSInAppApi>()
        }
    }

    /**
     * Enables tracking with the provided configuration.
     *
     * @param jsEmarsysConfig The SDK configuration to use for enabling tracking.
     * @return A promise that resolves when tracking is enabled.
     */
    fun enableTracking(jsEmarsysConfig: JsEmarsysConfig): Promise<Unit> {
        return applicationScope.promise {
            Emarsys.enableTracking(jsEmarsysConfig)
        }
    }

    /**
     * Disables tracking.
     *
     */
    fun disableTracking(): Promise<Unit> {
        return applicationScope.promise {
            Emarsys.disableTracking()
        }
    }
}