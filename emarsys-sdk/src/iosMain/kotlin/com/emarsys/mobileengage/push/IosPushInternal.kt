package com.emarsys.mobileengage.push

import com.emarsys.api.AppEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushInternal
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.networking.clients.push.PushClientApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

class IosPushInternal(
    pushClient: PushClientApi,
    storage: TypedStorageApi<String?>,
    pushContext: ApiContext<PushCall>,
    override val notificationEvents: MutableSharedFlow<AppEvent>
) : PushInternal(pushClient, storage, pushContext, notificationEvents), IosPushInstance {
    override var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol? = null

    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol = InternalNotificationCenterDelegateProxy(
        willPresentNotification = this::willPresentNotification,
        didReceiveNotificationResponse = this::didReceiveNotificationResponse,
        openSettingsForNotification = this::openSettingsForNotification
    )

    private fun willPresentNotification(
        center: UNUserNotificationCenter,
        notification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        customerUserNotificationCenterDelegate?.let {
            CoroutineScope(Dispatchers.Main).launch {
                it.userNotificationCenter(center, notification, withCompletionHandler)
            }
        }
        withCompletionHandler(UNNotificationPresentationOptionBanner + UNNotificationPresentationOptionList)
    }

    private fun didReceiveNotificationResponse(
        center: UNUserNotificationCenter,
        notificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        customerUserNotificationCenterDelegate?.let {
            CoroutineScope(Dispatchers.Main).launch {
                it.userNotificationCenter(
                    center,
                    notificationResponse,
                    withCompletionHandler
                )
            }
        }
    }

    private fun openSettingsForNotification(
        center: UNUserNotificationCenter,
        notification: UNNotification?
    ) {
        customerUserNotificationCenterDelegate?.let {
            CoroutineScope(Dispatchers.Main).launch {
                it.userNotificationCenter(center, notification)
            }
        }
    }

    private class InternalNotificationCenterDelegateProxy(
        private val willPresentNotification: (center: UNUserNotificationCenter, notification: UNNotification, handler: (UNNotificationPresentationOptions) -> Unit) -> Unit,
        private val didReceiveNotificationResponse: (center: UNUserNotificationCenter, response: UNNotificationResponse, handler: () -> Unit) -> Unit,
        private val openSettingsForNotification: (center: UNUserNotificationCenter, notification: UNNotification?) -> Unit): UNUserNotificationCenterDelegateProtocol, NSObject() {

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            willPresentNotification: UNNotification,
            withCompletionHandler: (UNNotificationPresentationOptions) -> Unit) {
            this.willPresentNotification(center, willPresentNotification, withCompletionHandler)
        }

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            didReceiveNotificationResponse: UNNotificationResponse,
            withCompletionHandler: () -> Unit) {
            this.didReceiveNotificationResponse(center, didReceiveNotificationResponse, withCompletionHandler)
        }

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            openSettingsForNotification: UNNotification?) {
            this.openSettingsForNotification(center, openSettingsForNotification)
        }
    }
}
