package com.emarsys.api.push

import Activatable
import SdkContext
import com.emarsys.api.generic.GenericApi
import kotlinx.coroutines.launch

interface PushInstance : PushApi, Activatable

class Push<Logging : PushInstance, Gatherer : PushInstance, Internal : PushInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContext
) : GenericApi<Logging, Gatherer, Internal>(loggingApi, gathererApi, internalApi, sdkContext), PushApi {
    override suspend fun setPushToken(pushToken: String) {
        sdkContext.sdkScope.launch {
            activeInstance<PushApi>().setPushToken(pushToken)
        }
    }

    override suspend fun clearPushToken() {
        sdkContext.sdkScope.launch {
            activeInstance<PushApi>().clearPushToken()
        }
    }
}