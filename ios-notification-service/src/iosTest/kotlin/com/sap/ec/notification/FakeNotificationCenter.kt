package com.sap.ec.iosNotificationService.notification

import platform.UserNotifications.UNNotificationCategory

class FakeNotificationCenter: NotificationCenterApi {
    override suspend fun addCategory(category: UNNotificationCategory) {
    }
}
