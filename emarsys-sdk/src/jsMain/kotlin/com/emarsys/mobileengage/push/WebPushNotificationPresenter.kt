package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.push.model.WebPushNotificationData
import com.emarsys.self

class WebPushNotificationPresenter: WebPushNotificationPresenterApi {

    override suspend fun showNotification(
        webPushNotificationData: WebPushNotificationData
    ) {
        self.registration.showNotification(
            webPushNotificationData.title,
            webPushNotificationData.options.unsafeCast<web.notifications.NotificationOptions>()
        )
    }

}