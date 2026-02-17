package com.sap.ec.mobileengage.push

import com.sap.ec.mobileengage.push.model.WebPushNotificationData

interface PushBroadcasterApi {

    fun broadcast(pushData: WebPushNotificationData)
}