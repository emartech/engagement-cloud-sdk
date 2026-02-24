package com.sap.ec.api.push

import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.push.IosPushWrapperApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

class IosPush : IosPushApi {

    override val registeredNotificationCenterDelegates: List<NotificationCenterDelegateRegistration>
        get() = koin.get<IosPushWrapperApi>().registeredNotificationCenterDelegates

    override fun registerNotificationCenterDelegate(
        delegate: UNUserNotificationCenterDelegateProtocol,
        options: NotificationCenterDelegateRegistrationOptions
    ) {
        koin.get<IosPushWrapperApi>().registerNotificationCenterDelegate(delegate, options)
    }

    override fun unregisterNotificationCenterDelegate(delegate: UNUserNotificationCenterDelegateProtocol) {
        koin.get<IosPushWrapperApi>().unregisterNotificationCenterDelegate(delegate)
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