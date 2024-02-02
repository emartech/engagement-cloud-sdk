package com.emarsys.api.push

import Activatable
import com.emarsys.api.AppEvent
import com.emarsys.api.SdkResult
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext

interface PushInstance : PushInternalApi, Activatable

class Push<Logging : PushInstance, Gatherer : PushInstance, Internal : PushInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(loggingApi, gathererApi, internalApi, sdkContext), PushInternalApi,
    PushApi {
    override val notificationEvents: MutableSharedFlow<AppEvent> = activeInstance<PushInstance>().notificationEvents

    override suspend fun registerPushToken(pushToken: String): SdkResult {
        return withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushInternalApi>().registerPushToken(pushToken)
        }
    }

    override suspend fun clearPushToken(): SdkResult {
        return withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushInternalApi>().clearPushToken()
        }
    }

    override val pushToken: String?
        get() = activeInstance<PushInternalApi>().pushToken

}