package com.emarsys

import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.mobileengage.push.PushMessageMapper
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import web.broadcast.BroadcastChannel
import web.events.EventType
import web.push.PushEvent
import web.serviceworker.NotificationEvent


fun main() {
    val pushBroadcastChannel = BroadcastChannel("emarsys-service-worker-push-channel")



    val sdkLogger = SdkLogger(ConsoleLogger())
    val pushMessagePresenter = PushMessagePresenter()
    val pushMessageMapper = PushMessageMapper(JsonUtil.json, sdkLogger)

    val emarsysServiceWorker = EmarsysServiceWorker(
        pushMessagePresenter,
        pushMessageMapper,
        CoroutineScope(SupervisorJob()),
        SdkLogger(ConsoleLogger())
    )


    self.addEventListener(EventType("push"), { event: PushEvent ->
        event.data?.let {
            val showNotificationPromise =
                emarsysServiceWorker.onPush(JSON.stringify(it.json()))
            event.waitUntil(showNotificationPromise)
        }

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