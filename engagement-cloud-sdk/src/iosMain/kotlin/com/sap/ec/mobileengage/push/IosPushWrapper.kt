package com.sap.ec.mobileengage.push

import com.sap.ec.api.generic.GenericApi
import com.sap.ec.api.push.PushInternalApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException.PreconditionFailedException
import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.push.extension.toSilentPushUserInfo
import com.sap.ec.util.JsonUtil
import kotlinx.coroutines.withContext
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

internal class IosPushWrapper<Logging : IosPushInstance, Gatherer : IosPushInstance, Internal : IosPushInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi,
    private val sdkLogger: Logger
) : GenericApi<Logging, Gatherer, Internal>(loggingApi, gathererApi, internalApi, sdkContext),
    IosPushWrapperApi {
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

    override var customerUserNotificationCenterDelegate: List<UNUserNotificationCenterDelegateProtocol>
        get() = activeInstance<IosPushInstance>().customerUserNotificationCenterDelegate
        set(value) {
            activeInstance<IosPushInstance>().customerUserNotificationCenterDelegate =
                value
        }

    override val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() = activeInstance<IosPushInstance>().userNotificationCenterDelegate

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
