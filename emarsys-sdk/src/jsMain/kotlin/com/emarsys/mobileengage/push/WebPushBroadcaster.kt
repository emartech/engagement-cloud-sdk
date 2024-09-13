package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.push.model.WebPushNotificationData
import org.w3c.dom.BroadcastChannel

class WebPushBroadcaster(
    private val processedPushBroadcastChannel: BroadcastChannel
) : PushBroadcasterApi {

    override fun broadcast(pushData: WebPushNotificationData) {
        processedPushBroadcastChannel.postMessage(JSON.stringify(pushData))
    }
}