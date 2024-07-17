package com.emarsys.mobileengage.push

import com.emarsys.api.AppEvent
import com.emarsys.api.push.PushInformation
import com.emarsys.api.push.PushInternalApi
import com.emarsys.api.push.PushType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

class PushOfficer: PushInternalApi, UNUserNotificationCenterDelegateProtocol, NSObject() {

    private val _pushInformation = MutableSharedFlow<PushInformation>()
    val pushInformation = _pushInformation.asSharedFlow()

    var userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol? = null

    override suspend fun registerPushToken(pushToken: String) {
        TODO("Not yet implemented")
    }

    override suspend fun clearPushToken() {
        TODO("Not yet implemented")
    }

    override val pushToken: String?
        get() = TODO("Not yet implemented")
    override val notificationEvents: MutableSharedFlow<AppEvent>
        get() = TODO("Not yet implemented")

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit) {
        if (userNotificationCenterDelegate != null) CoroutineScope(Dispatchers.Main).run {
            userNotificationCenterDelegate!!.userNotificationCenter(center, willPresentNotification, withCompletionHandler)
        }
        withCompletionHandler(UNNotificationPresentationOptionBanner + UNNotificationPresentationOptionList)
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit) {
        if (userNotificationCenterDelegate != null) CoroutineScope(Dispatchers.Main).run {
            userNotificationCenterDelegate!!.userNotificationCenter(center, didReceiveNotificationResponse, withCompletionHandler)
        }
        val userInfo = didReceiveNotificationResponse.notification.request.content.userInfo
        val ems = userInfo["ems"] as? Map<String, Any>
        ems?.get("multichannelId")?.let {
            _pushInformation.tryEmit(PushInformation(it as String, PushType.Push))
        }
        ems?.get("inapp")?.let {
            // TODO: show inApp message
        }

    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        openSettingsForNotification: UNNotification?) {
        if (userNotificationCenterDelegate != null) CoroutineScope(Dispatchers.Main).run {
            userNotificationCenterDelegate!!.userNotificationCenter(center, openSettingsForNotification)
        }
    }
}
