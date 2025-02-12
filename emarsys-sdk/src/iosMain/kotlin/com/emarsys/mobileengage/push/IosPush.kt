package com.emarsys.mobileengage.push

import com.emarsys.api.generic.GenericApi
import com.emarsys.api.push.PushInternalApi
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.withContext
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

class IosPush<Logging : IosPushInstance, Gatherer : IosPushInstance, Internal : IosPushInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(loggingApi, gathererApi, internalApi, sdkContext),
    IosPushApi {
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

    override val pushToken: Result<String?>
        get() = runCatching { activeInstance<PushInternalApi>().pushToken }

    override var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol?
        get() = activeInstance<IosPushInstance>().customerUserNotificationCenterDelegate
        set(value) {
            activeInstance<IosPushInstance>().customerUserNotificationCenterDelegate = value
        }
    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() = activeInstance<IosPushInstance>().emarsysUserNotificationCenterDelegate

    override suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>) {
    }
}
