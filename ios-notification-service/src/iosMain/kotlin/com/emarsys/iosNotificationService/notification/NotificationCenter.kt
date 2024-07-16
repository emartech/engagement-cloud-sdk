package com.emarsys.iosNotificationService.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

interface NotificationCenterApi {
    suspend fun addCategory(category: UNNotificationCategory)
}

class NotificationCenter: NotificationCenterApi {

    override suspend fun addCategory(category: UNNotificationCategory) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

        val categories = notificationCenter.notificationCategories()
        categories.add(category)
        notificationCenter.setNotificationCategories(categories)
    }

}

suspend fun UNUserNotificationCenter.notificationCategories(): MutableSet<UNNotificationCategory> =
    suspendCancellableCoroutine { continuation ->
        getNotificationCategoriesWithCompletionHandler { categories ->
            continuation.resume((categories as Set<UNNotificationCategory>).toMutableSet())
        }
    }