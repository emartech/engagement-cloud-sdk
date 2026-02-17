package com.sap.ec.mobileengage.push

import com.sap.ec.api.push.PushInstance
import com.sap.ec.api.push.SilentPushUserInfo
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

interface IosPushInstance: PushInstance {
    var customerUserNotificationCenterDelegate: List<UNUserNotificationCenterDelegateProtocol>
    val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol

    suspend fun handleSilentMessageWithUserInfo(userInfo: SilentPushUserInfo)
}