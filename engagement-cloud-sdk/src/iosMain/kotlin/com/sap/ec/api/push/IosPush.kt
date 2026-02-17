package com.sap.ec.api.push

import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.push.IosPushWrapperApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

class IosPush : IosPushApi {
    override var customerUserNotificationCenterDelegate: List<UNUserNotificationCenterDelegateProtocol>
        get() {
            return koin.get<IosPushWrapperApi>().customerUserNotificationCenterDelegate
        }
        set(value) {
            koin.get<IosPushWrapperApi>().customerUserNotificationCenterDelegate = value
        }

    override val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() = koin.get<IosPushWrapperApi>().userNotificationCenterDelegate

    override suspend fun getToken(): String? {
        return koin.get<IosPushWrapperApi>().getPushToken().getOrNull()
    }

    override suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>) {
        koin.get<IosPushWrapperApi>().handleSilentMessageWithUserInfo(rawUserInfo)
    }

    override suspend fun registerToken(token: String) {
        koin.get<IosPushWrapperApi>().registerPushToken(token)
    }

    override suspend fun clearToken() {
        koin.get<IosPushWrapperApi>().clearPushToken()
    }
}