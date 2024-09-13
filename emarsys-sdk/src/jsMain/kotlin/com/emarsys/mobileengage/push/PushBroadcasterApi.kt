package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.push.model.WebPushNotificationData

interface PushBroadcasterApi {

    fun broadcast(pushData: WebPushNotificationData)
}