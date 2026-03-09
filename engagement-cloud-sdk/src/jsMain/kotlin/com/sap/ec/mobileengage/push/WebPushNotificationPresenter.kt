package com.sap.ec.mobileengage.push

import com.sap.ec.InternalSdkApi
import com.sap.ec.mobileengage.push.model.WebPushNotificationData
import com.sap.ec.self
import web.serviceworker.showNotification


internal class WebPushNotificationPresenter: WebPushNotificationPresenterApi {

    override suspend fun showNotification(
        webPushNotificationData: WebPushNotificationData
    ) {
        self.registration.showNotification(
            webPushNotificationData.title,
            webPushNotificationData.options.unsafeCast<web.notifications.NotificationOptions>()
        )
    }

}