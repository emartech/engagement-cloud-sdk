package com.sap.ec.mobileengage.push

import com.sap.ec.InternalSdkApi
import com.sap.ec.mobileengage.push.model.WebPushNotificationData

@InternalSdkApi
interface WebPushNotificationPresenterApi {

    suspend fun showNotification(webPushNotificationData: WebPushNotificationData)

}
