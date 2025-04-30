package com.emarsys.mobileengage.push

import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.SilentPushUserInfo
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

interface IosPushInstance: PushInstance {
    var customerUserNotificationCenterDelegate: List<UNUserNotificationCenterDelegateProtocol>
    val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol

    suspend fun handleSilentMessageWithUserInfo(userInfo: SilentPushUserInfo)
}