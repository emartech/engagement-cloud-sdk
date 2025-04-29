package com.emarsys.api.push

import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.push.IosPushWrapperApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

class IosPush : IosPushApi {
    override var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol?
        get() {
            return koin.get<IosPushWrapperApi>().customerUserNotificationCenterDelegate
        }
        set(value) {
            koin.get<IosPushWrapperApi>().customerUserNotificationCenterDelegate = value
        }

    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() = koin.get<IosPushWrapperApi>().emarsysUserNotificationCenterDelegate

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