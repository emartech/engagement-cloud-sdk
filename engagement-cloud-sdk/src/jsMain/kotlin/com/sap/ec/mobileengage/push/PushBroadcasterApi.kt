package com.sap.ec.mobileengage.push

import com.sap.ec.mobileengage.push.model.WebPushNotificationData

internal interface PushBroadcasterApi {

    fun broadcast(pushData: WebPushNotificationData)
}