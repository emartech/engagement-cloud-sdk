package com.emarsys.mobileengage.push

import com.emarsys.api.generic.GenericApi
import com.emarsys.api.push.PushInternalApi
import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.PreconditionFailedException
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.push.extension.toSilentPushUserInfo
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.withContext
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

class IosPush<Logging : IosPushInstance, Gatherer : IosPushInstance, Internal : IosPushInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi,
    private val sdkLogger: Logger
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

    override suspend fun getPushToken(): Result<String?> {
        return kotlin.runCatching { activeInstance<PushInternalApi>().getPushToken() }
    }

    override var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol?
        get() = activeInstance<IosPushInstance>().customerUserNotificationCenterDelegate
        set(value) {
            activeInstance<IosPushInstance>().customerUserNotificationCenterDelegate =
                value
        }

    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() = activeInstance<IosPushInstance>().emarsysUserNotificationCenterDelegate

    override suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>): Result<Unit> =
        runCatching {
            withContext(sdkContext.sdkDispatcher) {
                try {
                    val pushUserInfo = rawUserInfo.toSilentPushUserInfo(JsonUtil.json)
                    pushUserInfo?.let {
                        activeInstance<IosPushInstance>().handleSilentMessageWithUserInfo(
                            pushUserInfo
                        )
                    }
                } catch (e: Exception) {
                    sdkLogger.error("IosPush - handleSilentMessageWithUserInfo", e)
                    throw PreconditionFailedException("Error while handling silent push message, the userInfo can't be parsed")
                }
            }
        }
}
