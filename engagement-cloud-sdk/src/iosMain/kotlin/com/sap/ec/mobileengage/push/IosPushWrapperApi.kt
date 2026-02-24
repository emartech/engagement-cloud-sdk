package com.sap.ec.mobileengage.push


import com.sap.ec.api.push.NotificationCenterDelegateRegistration
import com.sap.ec.api.push.NotificationCenterDelegateRegistrationOptions
import com.sap.ec.api.push.PushApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

internal interface IosPushWrapperApi : PushApi {
    val registeredNotificationCenterDelegates: List<NotificationCenterDelegateRegistration>
    val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol

    fun registerNotificationCenterDelegate(
        delegate: UNUserNotificationCenterDelegateProtocol,
        options: NotificationCenterDelegateRegistrationOptions = NotificationCenterDelegateRegistrationOptions()
    )

    fun unregisterNotificationCenterDelegate(delegate: UNUserNotificationCenterDelegateProtocol)

    suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>): Result<Unit>
}