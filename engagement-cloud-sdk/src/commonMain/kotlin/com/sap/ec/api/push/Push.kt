package com.sap.ec.api.push

import Activatable
import com.sap.ec.api.generic.GenericApi
import com.sap.ec.context.SdkContextApi
import kotlinx.coroutines.withContext

interface PushInstance : PushInternalApi, Activatable

internal class Push<Logging : PushInstance, Gatherer : PushInstance, Internal : PushInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(loggingApi, gathererApi, internalApi, sdkContext),
    PushApi {
    override suspend fun registerPushToken(pushToken: String): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushInternalApi>().registerPushToken(pushToken)
        }
    }

    override suspend fun clearPushToken(): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushInternalApi>().clearPushToken()
        }
    }

    override suspend fun getPushToken(): Result<String?> {
        return runCatching { activeInstance<PushInternalApi>().getPushToken() }
    }
}