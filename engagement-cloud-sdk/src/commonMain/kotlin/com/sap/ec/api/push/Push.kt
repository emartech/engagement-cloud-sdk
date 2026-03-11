package com.sap.ec.api.push

import com.sap.ec.api.Activatable
import com.sap.ec.api.generic.GenericApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.util.runCatchingWithoutCancellation
import kotlinx.coroutines.withContext

internal interface PushInstance : PushInternalApi, Activatable

internal class Push<Logging : PushInstance, Gatherer : PushInstance, Internal : PushInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(loggingApi, gathererApi, internalApi, sdkContext),
    PushApi {
    override suspend fun registerPushToken(pushToken: String): Result<Unit> = runCatchingWithoutCancellation {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushInternalApi>().registerPushToken(pushToken)
        }
    }

    override suspend fun clearPushToken(): Result<Unit> = runCatchingWithoutCancellation {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushInternalApi>().clearPushToken()
        }
    }

    override suspend fun getPushToken(): Result<String?> {
        return runCatchingWithoutCancellation { activeInstance<PushInternalApi>().getPushToken() }
    }
}