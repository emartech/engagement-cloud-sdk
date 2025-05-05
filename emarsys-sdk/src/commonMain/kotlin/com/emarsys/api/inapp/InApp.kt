package com.emarsys.api.inapp

import Activatable
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.withContext

interface InAppInstance : InAppInternalApi, Activatable

internal class InApp<Logging : InAppInstance, Gatherer : InAppInstance, Internal : InAppInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(
    loggingApi, gathererApi, internalApi, sdkContext
), InAppApi {
    override suspend fun pause(): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<InAppInstance>().pause()
        }
    }

    override suspend fun resume(): Result<Unit> = runCatching {
         withContext(sdkContext.sdkDispatcher) {
            activeInstance<InAppInstance>().resume()
        }
    }

    override val isPaused: Boolean
        get() = activeInstance<InAppInstance>().isPaused
}