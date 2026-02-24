package com.sap.ec.mobileengage.push

import com.sap.ec.api.push.NotificationCenterDelegateRegistration
import com.sap.ec.api.push.NotificationCenterDelegateRegistrationOptions
import com.sap.ec.api.push.PushInstance
import com.sap.ec.api.push.SilentPushUserInfo
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

interface IosPushInstance : PushInstance {
    val registeredNotificationCenterDelegates: List<NotificationCenterDelegateRegistration>
    val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol

    fun registerNotificationCenterDelegate(
        delegate: UNUserNotificationCenterDelegateProtocol,
        options: NotificationCenterDelegateRegistrationOptions = NotificationCenterDelegateRegistrationOptions()
    )

    fun unregisterNotificationCenterDelegate(delegate: UNUserNotificationCenterDelegateProtocol)

    suspend fun handleSilentMessageWithUserInfo(userInfo: SilentPushUserInfo)
}