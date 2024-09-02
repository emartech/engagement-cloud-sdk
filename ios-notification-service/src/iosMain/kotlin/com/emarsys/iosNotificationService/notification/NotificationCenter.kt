package com.emarsys.iosNotificationService.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

interface NotificationCenterApi {
    suspend fun addCategory(category: UNNotificationCategory)
}

class NotificationCenter : NotificationCenterApi {

    override suspend fun addCategory(category: UNNotificationCategory) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

        val categories = notificationCenter.notificationCategories()
        categories.add(category)
        notificationCenter.setNotificationCategories(categories.toSet())
    }

}

suspend fun UNUserNotificationCenter.notificationCategories(): MutableSet<UNNotificationCategory> =
    suspendCancellableCoroutine { continuation ->
        getNotificationCategoriesWithCompletionHandler { categories ->
            val result =
                if (!categories.isNullOrEmpty()) emptySet<UNNotificationCategory>() else categories
            continuation.resume((result as Set<UNNotificationCategory>).toMutableSet())
        }
    }