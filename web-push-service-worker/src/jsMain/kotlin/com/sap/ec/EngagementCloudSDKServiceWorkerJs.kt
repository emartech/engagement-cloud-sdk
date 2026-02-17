package com.sap.ec

import com.sap.ec.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.sap.ec.api.push.PushConstants.WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME
import com.sap.ec.api.push.PushConstants.WEB_PUSH_SDK_READY_CHANNEL_NAME
import com.sap.ec.core.log.ConsoleLogger
import com.sap.ec.mobileengage.push.PushMessagePresenter
import com.sap.ec.mobileengage.push.WebPushNotificationPresenter
import com.sap.ec.mobileengage.push.mappers.PushMessageWebV2Mapper
import com.sap.ec.mobileengage.push.model.JsNotificationClickedData
import com.sap.ec.mobileengage.push.model.JsPushMessage
import com.sap.ec.notification.NotificationClickHandler
import com.sap.ec.util.JsonUtil
import com.sap.ec.window.BrowserWindowHandler
import js.coroutines.promise
import js.promise.Promise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import web.broadcast.BroadcastChannel
import web.events.EventHandler
import web.events.EventType
import web.events.addEventListener
import web.push.PushEvent
import web.serviceworker.NotificationEvent
import kotlin.js.Promise.Companion.reject
import kotlin.js.Promise.Companion.resolve


@OptIn(ExperimentalWasmJsInterop::class)
fun main() {
    val serviceWorkerScope = CoroutineScope(SupervisorJob())

    val onNotificationClickedBroadcastChannel =
        BroadcastChannel(WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME)
    val onBadgeCountUpdateReceivedBroadcastChannel =
        BroadcastChannel(WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED)
    val sdkReadyBroadcastChannel = BroadcastChannel(WEB_PUSH_SDK_READY_CHANNEL_NAME)

    val consoleLogger = ConsoleLogger()
    val pushMessagePresenter = PushMessagePresenter(WebPushNotificationPresenter())
    val pushMessageWebV2Mapper =
        PushMessageWebV2Mapper(JsonUtil.json, consoleLogger)

    val engagementCloudServiceWorker = EngagementCloudServiceWorker(
        pushMessagePresenter,
        pushMessageWebV2Mapper,
        onBadgeCountUpdateReceivedBroadcastChannel,
        JsonUtil.json,
        consoleLogger
    )

    val notificationClickHandler = NotificationClickHandler(
        onNotificationClickedBroadcastChannel,
        BrowserWindowHandler()
    )


    self.addEventListener(EventType("push"), { event: PushEvent ->
        event.data?.let {
            val showNotificationPromise = CoroutineScope(SupervisorJob()).promise {
                engagementCloudServiceWorker.onPush(JSON.stringify(it.json()))
            }
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
        event.waitUntil(Promise<Unit> { resolve, reject ->
            serviceWorkerScope.launch {
                try {
                    notificationClickHandler.handleNotificationClick(jsNotificationClickedData)
                    event.notification.close()
                    resolve(Unit)
                } catch (e: Exception) {
                    reject(e)
                }
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