package com.sap.ec.mobileengage.push

import com.sap.ec.mobileengage.push.model.WebPushNotificationData

interface WebPushNotificationPresenterApi {

    suspend fun showNotification(webPushNotificationData: WebPushNotificationData)

}
