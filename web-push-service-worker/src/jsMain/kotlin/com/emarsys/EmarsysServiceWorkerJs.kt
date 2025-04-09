package com.emarsys

import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME
import com.emarsys.api.push.PushConstants.WEB_PUSH_SDK_READY_CHANNEL_NAME
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.WebPushNotificationPresenter
import com.emarsys.mobileengage.push.mappers.PushMessageMapper
import com.emarsys.mobileengage.push.mappers.PushMessageWebV1Mapper
import com.emarsys.mobileengage.push.model.JsNotificationClickedData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.notification.NotificationClickHandler
import com.emarsys.util.JsonUtil
import com.emarsys.window.BrowserWindowHandler
import js.promise.Promise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.promise
import web.broadcast.BroadcastChannel
import web.events.EventHandler
import web.events.EventType
import web.events.addEventListener
import web.push.PushEvent
import web.serviceworker.NotificationEvent


fun main() {
    val serviceWorkerScope = CoroutineScope(SupervisorJob())

    val onNotificationClickedBroadcastChannel =
        BroadcastChannel(WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME)
    val onBadgeCountUpdateReceivedBroadcastChannel =
        BroadcastChannel(WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED)
    val sdkReadyBroadcastChannel = BroadcastChannel(WEB_PUSH_SDK_READY_CHANNEL_NAME)

    val consoleLogger = ConsoleLogger()
    val pushMessagePresenter = PushMessagePresenter(WebPushNotificationPresenter())
    val pushMessageMapper =
        PushMessageMapper(JsonUtil.json, SdkLogger("PushMessageMapper", consoleLogger))
    val pushMessageWebV1Mapper =
        PushMessageWebV1Mapper(JsonUtil.json, SdkLogger("PushMessageWebV1Mapper", consoleLogger))

    val emarsysServiceWorker = EmarsysServiceWorker(
        pushMessagePresenter,
        pushMessageMapper,
        pushMessageWebV1Mapper,
        onBadgeCountUpdateReceivedBroadcastChannel,
        JsonUtil.json,
        CoroutineScope(SupervisorJob()),
        SdkLogger("EmarsysServiceWorker", consoleLogger)
    )

    val notificationClickHandler = NotificationClickHandler(
        onNotificationClickedBroadcastChannel,
        BrowserWindowHandler()
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
        val jsPushMessage =
            JsonUtil.json.decodeFromString<JsPushMessage>(event.notification.data.unsafeCast<String>())
        val jsNotificationClickedData = JsonUtil.json.encodeToString(
            JsNotificationClickedData(
                event.action.unsafeCast<String?>() ?: "",
                jsPushMessage
            )
        )
        event.waitUntil(Promise { resolve, reject ->
            serviceWorkerScope.promise {
                notificationClickHandler.handleNotificationClick(jsNotificationClickedData)
                event.notification.close()
            }.then {
                resolve(Unit)
            }.catch {
                reject(it)
            }
        })
    })

    sdkReadyBroadcastChannel.onmessage = EventHandler {
        notificationClickHandler.postStoredMessageToSDK()
    }

    self.addEventListener(EventType("pushsubscriptionchange"), {
        console.log("pushsubscriptionchange")
    })
}