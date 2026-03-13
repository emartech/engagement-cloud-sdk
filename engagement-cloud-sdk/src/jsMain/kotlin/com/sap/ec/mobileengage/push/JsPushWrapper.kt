package com.sap.ec.mobileengage.push

import com.sap.ec.api.generic.GenericApi
import com.sap.ec.api.push.PushInternalApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.util.runCatchingWithoutCancellation
import kotlinx.coroutines.withContext

internal class JsPushWrapper<Logging : JsPushInstance, Gatherer : JsPushInstance, Internal : JsPushInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi,
) : GenericApi<Logging, Gatherer, Internal>(loggingApi, gathererApi, internalApi, sdkContext),
    JsPushWrapperApi {

    override suspend fun subscribe(): Result<Unit> {
        return withContext(sdkContext.sdkDispatcher) {
            activeInstance<JsPushInstance>().subscribe()
        }
    }

    override suspend fun unsubscribe(): Result<Unit> {
        return withContext(sdkContext.sdkDispatcher) {
            activeInstance<JsPushInstance>().unsubscribe()
        }
    }

    override suspend fun isSubscribed(): Boolean {
        return withContext(sdkContext.sdkDispatcher) {
            activeInstance<JsPushInstance>().isSubscribed()
        }
    }

    override suspend fun getPermissionState(): String {
        return withContext(sdkContext.sdkDispatcher) {
            activeInstance<JsPushInstance>().getPermissionState().name.lowercase()
        }
    }

    override suspend fun registerToken(token: String): Result<Unit> =
        runCatchingWithoutCancellation {
            withContext(sdkContext.sdkDispatcher) {
                activeInstance<PushInternalApi>().registerPushToken(token)
            }
        }

    override suspend fun clearToken(): Result<Unit> =
        runCatchingWithoutCancellation {
            withContext(sdkContext.sdkDispatcher) {
                activeInstance<PushInternalApi>().clearPushToken()
            }
        }


    override suspend fun getToken(): Result<String?> = runCatchingWithoutCancellation {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<PushInternalApi>().getPushToken()
        }
    }
}