package com.emarsys.mobileengage.push

import com.emarsys.api.push.PushInstance
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

interface IosPushInstance: PushInstance {
    var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol?
    val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol

    fun registerEmarsysNotificationCenterDelegate()
    suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>)
}