package com.emarsys.mobileengage.push


import com.emarsys.api.push.PushApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

interface IosPushApi: PushApi {
    var customerUserNotificationCenterDelegate: Result<UNUserNotificationCenterDelegateProtocol?>
    val emarsysUserNotificationCenterDelegate: Result<UNUserNotificationCenterDelegateProtocol>

    suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>): Result<Unit>
}