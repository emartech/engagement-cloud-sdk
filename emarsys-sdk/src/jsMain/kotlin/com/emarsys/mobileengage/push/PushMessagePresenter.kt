package com.emarsys.mobileengage.push


import com.emarsys.mobileengage.push.model.JsNotificationAction
import com.emarsys.mobileengage.push.model.JsNotificationOptions
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import org.w3c.dom.BroadcastChannel
import org.w3c.notifications.NotificationAction
import org.w3c.notifications.NotificationOptions

open class PushMessagePresenter : PushPresenter<JsPlatformData, JsPushMessage> {
    val processedPushBroadcastChannel =
        BroadcastChannel("emarsys-service-worker-processed-push-channel")

    override suspend fun present(pushMessage: JsPushMessage) {
        val jsNotificationOptions = JsNotificationOptions(
            body = pushMessage.body,
            icon = pushMessage.iconUrlString,
            badge = pushMessage.imageUrlString,
            actions = pushMessage.data.actions?.map {
                JsNotificationAction(
                    action = it.id,
                    title = it.title
                )
            } ?: emptyList(),
            pushToInApp = pushMessage.data.pushToInApp
        )

        broadcastNotificationData(
            pushMessage.title,
            jsNotificationOptions
        )
    }

    private fun broadcastNotificationData(
        notificationTitle: String,
        jsNotificationOptions: JsNotificationOptions
    ) {
        val notificationOptions = js("{}").unsafeCast<NotificationOptions>().apply {
            body = jsNotificationOptions.body
            icon = jsNotificationOptions.icon
            badge = jsNotificationOptions.badge
        }

        notificationOptions.asDynamic().actions = notificationActions(jsNotificationOptions)

        jsNotificationOptions.pushToInApp?.let { pushToInApp ->
            val data = js("{}")
            data["campaignId"] = pushToInApp.campaignId
            data["url"] = pushToInApp.url
            data["ignoreViewedEvent"] = pushToInApp.ignoreViewedEvent

            notificationOptions.asDynamic().data = data
        }

        val processedPushMessage = js("{}")
        processedPushMessage["title"] = notificationTitle
        processedPushMessage["options"] = notificationOptions

        processedPushBroadcastChannel.postMessage(JSON.stringify(processedPushMessage))
    }

    private fun notificationActions(jsNotificationOptions: JsNotificationOptions) =
        jsNotificationOptions.actions.map {
            js("{}").unsafeCast<NotificationAction>().apply {
                action = it.action
                title = it.title
            }
        }

}