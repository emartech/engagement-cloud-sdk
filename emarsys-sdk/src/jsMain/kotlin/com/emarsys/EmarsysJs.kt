package com.emarsys

import com.emarsys.api.contact.JSContactApi
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
    lateinit var contact: JSContactApi
    lateinit var tracking: JSTrackingApi

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     *
     * @return A promise that resolves when the initialization is complete.
     */
    fun init(): Promise<Any> {
        return CoroutineScope(SupervisorJob()).promise {
            Emarsys.initialize()
            applicationScope = koin.get<CoroutineScope>(named(CoroutineScopeTypes.Application))
            contact = koin.get<JSContactApi>()
            tracking = koin.get<JSTrackingApi>()
        }
    }

    /**
     * Enables tracking with the provided configuration.
     *
     * @param jsEmarsysConfig The SDK configuration to use for enabling tracking.
     * @return A promise that resolves when tracking is enabled.
     */
    fun enableTracking(jsEmarsysConfig: JsEmarsysConfig): Promise<Any> {
        return applicationScope.promise {
            Emarsys.enableTracking(jsEmarsysConfig)
        }
    }

    /**
     * Disables tracking.
     *
     */
    fun disableTracking(): Promise<Any> {
        return applicationScope.promise {
            Emarsys.disableTracking()
        }
    }
}