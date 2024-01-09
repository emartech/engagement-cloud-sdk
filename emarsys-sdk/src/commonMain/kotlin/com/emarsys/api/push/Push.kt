package com.emarsys.api.push

import Activatable
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.withContext

interface PushInstance : PushApi, Activatable

class Push<Logging : PushInstance, Gatherer : PushInstance, Internal : PushInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(loggingApi, gathererApi, internalApi, sdkContext), PushApi {
    override suspend fun registerPushToken(pushToken: String) {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushApi>().registerPushToken(pushToken)
        }
    }

    override suspend fun clearPushToken() {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushApi>().clearPushToken()
        }
    }
}