package com.sap.ec.mobileengage.push


import com.sap.ec.api.push.PushApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

internal interface IosPushWrapperApi: PushApi {
    var customerUserNotificationCenterDelegate: List<UNUserNotificationCenterDelegateProtocol>
    val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol

    suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>): Result<Unit>
}