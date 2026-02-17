package com.sap.ec.mobileengage.push

import com.sap.ec.mobileengage.push.model.WebPushNotificationData
import org.w3c.dom.BroadcastChannel

class WebPushBroadcaster(
    private val processedPushBroadcastChannel: BroadcastChannel
) : PushBroadcasterApi {

    override fun broadcast(pushData: WebPushNotificationData) {
        processedPushBroadcastChannel.postMessage(JSON.stringify(pushData))
    }
}