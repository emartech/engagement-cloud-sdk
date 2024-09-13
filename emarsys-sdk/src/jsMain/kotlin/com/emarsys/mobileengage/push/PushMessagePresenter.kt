package com.emarsys.mobileengage.push


import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.inapp.PushToInApp
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.mobileengage.push.model.WebPushNotificationData
import org.w3c.notifications.NotificationAction
import org.w3c.notifications.NotificationOptions

open class PushMessagePresenter(private val pushBroadcaster: PushBroadcasterApi) :
    PushPresenter<JsPlatformData, JsPushMessage> {

    override suspend fun present(pushMessage: JsPushMessage) {
        val notificationOptions = js("{}").unsafeCast<NotificationOptions>().apply {
            body = pushMessage.body
            icon = pushMessage.iconUrlString
            badge = pushMessage.imageUrlString
        }

        pushMessage.data.actions?.let {
            notificationOptions.asDynamic().actions = createNotificationActions(it)
        }

        pushMessage.data.pushToInApp?.let {
            notificationOptions.asDynamic().data = createPushToInApp(it)
        }

        pushBroadcaster.broadcast(WebPushNotificationData(pushMessage.title, notificationOptions))
    }

    private fun createNotificationActions(actions: List<PresentableActionModel>) =
        actions.map {
            js("{}").unsafeCast<NotificationAction>().apply {
                action = it.id
                title = it.title
            }
        }

    private fun createPushToInApp(it: PushToInApp): dynamic {
        val data = js("{}")
        data["campaignId"] = it.campaignId
        data["url"] = it.url
        data["ignoreViewedEvent"] = it.ignoreViewedEvent
        return data
    }
}