package com.emarsys.api.push

import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.push.IosPushApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

class IosPublicPush : IosPublicPushApi {
    override var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol?
        get() {
            return koin.get<IosPushApi>().customerUserNotificationCenterDelegate
        }
        set(value) {
            koin.get<IosPushApi>().customerUserNotificationCenterDelegate = value
        }

    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() = koin.get<IosPushApi>().emarsysUserNotificationCenterDelegate

    override suspend fun getToken(): String? {
        return koin.get<IosPushApi>().getPushToken().getOrNull()
    }

    override suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>) {
        koin.get<IosPushApi>().handleSilentMessageWithUserInfo(rawUserInfo)
    }

    override suspend fun registerToken(token: String) {
        koin.get<IosPushApi>().registerPushToken(token)
    }

    override suspend fun clearToken() {
        koin.get<IosPushApi>().clearPushToken()
    }
}