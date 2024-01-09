package com.emarsys.api.push

import Activatable
import SdkContext
import com.emarsys.api.generic.GenericApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface PushInstance : PushApi, Activatable

class Push<Logging : PushInstance, Gatherer : PushInstance, Internal : PushInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContext
) : GenericApi<Logging, Gatherer, Internal>(loggingApi, gathererApi, internalApi, sdkContext), PushApi {
    override suspend fun setPushToken(pushToken: String) {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushApi>().setPushToken(pushToken)
        }
    }

    override suspend fun clearPushToken() {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushApi>().clearPushToken()
        }
    }
}