package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.push.model.WebPushNotificationData

interface WebPushNotificationPresenterApi {

    suspend fun showNotification(webPushNotificationData: WebPushNotificationData)

}
