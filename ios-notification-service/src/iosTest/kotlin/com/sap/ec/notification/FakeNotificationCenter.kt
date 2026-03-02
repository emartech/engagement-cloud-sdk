package com.sap.ec.notification

import platform.UserNotifications.UNNotificationCategory

class FakeNotificationCenter: NotificationCenterApi {
    override suspend fun addCategory(category: UNNotificationCategory) {
    }
}
