package com.sap.ec.mobileengage.push

import com.sap.ec.mobileengage.push.model.WebPushNotificationData
import com.sap.ec.self

class WebPushNotificationPresenter: WebPushNotificationPresenterApi {

    override suspend fun showNotification(
        webPushNotificationData: WebPushNotificationData
    ) {
        self.registration.asDynamic().showNotification(
            webPushNotificationData.title,
            webPushNotificationData.options.unsafeCast<web.notifications.NotificationOptions>()
        )
    }

}