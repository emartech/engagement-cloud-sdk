package com.emarsys.core.badge

import com.emarsys.core.device.UIDevice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.UIKit.UIApplication
import platform.UserNotifications.UNUserNotificationCenter

class IosBadgeCountHandler(
    private val notificationCenter: UNUserNotificationCenter,
    private val uiDevice: UIDevice,
    private val mainCoroutineDispatcher: CoroutineDispatcher
) : BadgeCountHandlerApi {

    override suspend fun add(increment: Int) {
        val currentBadgeCount =
            withContext(mainCoroutineDispatcher) { UIApplication.sharedApplication.applicationIconBadgeNumber.toInt() }
        set(currentBadgeCount + increment)
    }

    override suspend fun set(value: Int) {
        if (uiDevice hasOsVersionAtLeast 16) {
            notificationCenter.setBadgeCount(value.toLong(), null)
        } else {
            withContext(mainCoroutineDispatcher) {
                UIApplication.sharedApplication.applicationIconBadgeNumber = value.toLong()
            }
        }
    }

}