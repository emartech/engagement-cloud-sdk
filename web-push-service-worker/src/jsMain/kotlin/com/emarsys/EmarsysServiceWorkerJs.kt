package com.emarsys

import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.WebPushNotificationPresenter
import com.emarsys.mobileengage.push.mappers.PushMessageMapper
import com.emarsys.mobileengage.push.mappers.PushMessageWebV1Mapper
import com.emarsys.mobileengage.push.model.JsNotificationClickedData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.encodeToString
import web.broadcast.BroadcastChannel
import web.events.EventType
import web.push.PushEvent
import web.serviceworker.NotificationEvent


fun main() {
    val onNotificationClickedBroadcastChannel =
        BroadcastChannel(WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME)

    val sdkLogger = SdkLogger(ConsoleLogger())
    val pushMessagePresenter = PushMessagePresenter(WebPushNotificationPresenter())
    val pushMessageMapper = PushMessageMapper(JsonUtil.json, sdkLogger)
    val pushMessageWebV1Mapper = PushMessageWebV1Mapper(JsonUtil.json, sdkLogger)

    val emarsysServiceWorker = EmarsysServiceWorker(
        pushMessagePresenter,
        pushMessageMapper,
        pushMessageWebV1Mapper,
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
        // TODO implement opening window in case it is not open
        val jsPushMessage =
            JsonUtil.json.decodeFromString<JsPushMessage>(event.notification.data.unsafeCast<String>())
        onNotificationClickedBroadcastChannel.postMessage(
            JsonUtil.json.encodeToString(
                JsNotificationClickedData(
                    event.action,
                    jsPushMessage
                )
            )
        )
    })

    self.addEventListener(EventType("pushsubscriptionchange"), {
        console.log("pushsubscriptionchange")
    })
}