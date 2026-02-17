package com.sap.ec.core.permission

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class IosPermissionHandler(private val notificationCenter: UNUserNotificationCenter) :
    PermissionHandlerApi {

    override suspend fun requestPushPermission() {
        suspendCoroutine { continuation ->
            notificationCenter.requestAuthorizationWithOptions(
                UNAuthorizationOptionSound or UNAuthorizationOptionAlert or UNAuthorizationOptionBadge
            ) { granted, _ ->
                continuation.resume(granted)
            }
        }
    }
}
