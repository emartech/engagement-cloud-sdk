package com.emarsys.mobileengage.push


import com.emarsys.api.push.PushApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

internal interface IosPushWrapperApi: PushApi {
    var customerUserNotificationCenterDelegate: List<UNUserNotificationCenterDelegateProtocol>
    val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol

    suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>): Result<Unit>
}