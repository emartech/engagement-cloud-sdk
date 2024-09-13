package com.emarsys

import com.emarsys.mobileengage.push.model.WebPushNotificationData
import web.broadcast.BroadcastChannel
import web.events.EventType
import web.push.PushEvent
import web.serviceworker.NotificationEvent


fun main() {
    val pushBroadcastChannel = BroadcastChannel("emarsys-service-worker-push-channel")
    val processedPushBroadcastChannel =
        BroadcastChannel("emarsys-service-worker-processed-push-channel")

    self.addEventListener(EventType("push"), { event: PushEvent ->
        pushBroadcastChannel.postMessage(JSON.stringify(event.data?.json()))
    })

    self.addEventListener(EventType("install"), {
        console.log("install")
    })

    self.addEventListener(EventType("notificationclick"), { event: NotificationEvent ->
        console.log("notificationclick")
    })

    self.addEventListener(EventType("pushsubscriptionchange"), {
        console.log("pushsubscriptionchange")
    })

    processedPushBroadcastChannel.onmessage = { event ->
        val notificationData = JSON.parse<WebPushNotificationData>(event.data as String)

        self.registration.showNotification(
            notificationData.title,
            notificationData.options.asDynamic()
        )
    }
}