package com.sap.ec.api.inapp

import com.sap.ec.api.Activatable
import com.sap.ec.api.generic.GenericApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.util.runCatchingWithoutCancellation
import kotlinx.coroutines.withContext

internal interface InAppInstance : InAppInternalApi, Activatable

internal class InApp<Logging : InAppInstance, Gatherer : InAppInstance, Internal : InAppInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(
    loggingApi, gathererApi, internalApi, sdkContext
), InAppApi {
    override suspend fun pause(): Result<Unit> = runCatchingWithoutCancellation {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<InAppInstance>().pause()
        }
    }

    override suspend fun resume(): Result<Unit> = runCatchingWithoutCancellation {
         withContext(sdkContext.sdkDispatcher) {
            activeInstance<InAppInstance>().resume()
        }
    }

    override val isPaused: Boolean
        get() = activeInstance<InAppInstance>().isPaused
}