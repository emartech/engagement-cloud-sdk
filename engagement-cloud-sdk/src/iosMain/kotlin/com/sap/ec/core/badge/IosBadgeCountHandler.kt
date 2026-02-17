package com.sap.ec.core.badge

import com.sap.ec.core.actions.badge.BadgeCountHandlerApi
import com.sap.ec.core.device.UIDeviceApi
import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BadgeCountMethod.ADD
import com.sap.ec.mobileengage.action.models.BadgeCountMethod.SET
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.UIKit.UIApplication
import platform.UserNotifications.UNUserNotificationCenter

internal class IosBadgeCountHandler(
    private val notificationCenter: UNUserNotificationCenter,
    private val uiDevice: UIDeviceApi,
    private val mainCoroutineDispatcher: CoroutineDispatcher
) : BadgeCountHandlerApi {

    override suspend fun handle(badgeCount: BadgeCount) {
        when(badgeCount.method) {
            ADD -> add(badgeCount.value)
            SET -> set(badgeCount.value)
        }
    }

    private suspend fun add(increment: Int) {
        val currentBadgeCount =
            withContext(mainCoroutineDispatcher) { UIApplication.sharedApplication.applicationIconBadgeNumber.toInt() }
        set(currentBadgeCount + increment)
    }

    private suspend fun set(value: Int) {
        if (uiDevice hasOsVersionAtLeast 16) {
            notificationCenter.setBadgeCount(value.toLong(), null)
        } else {
            withContext(mainCoroutineDispatcher) {
                UIApplication.sharedApplication.applicationIconBadgeNumber = value.toLong()
            }
        }
    }

}