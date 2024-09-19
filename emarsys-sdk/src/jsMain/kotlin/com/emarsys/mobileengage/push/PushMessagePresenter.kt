package com.emarsys.mobileengage.push


import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.inapp.PushToInApp
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.self
import org.w3c.notifications.NotificationAction
import org.w3c.notifications.NotificationOptions

open class PushMessagePresenter :
    PushPresenter<JsPlatformData, JsPushMessage> {

    override suspend fun present(pushMessage: JsPushMessage) {
        val notificationOptions = js("{}").unsafeCast<NotificationOptions>().apply {
            body = pushMessage.body
            icon = pushMessage.iconUrlString
            badge = pushMessage.imageUrlString
        }

        pushMessage.data.actions?.let {
            notificationOptions.actions = createNotificationActions(it)
        }

        pushMessage.data.pushToInApp?.let {
            notificationOptions.data = createPushToInApp(it)
        }

        self.registration.showNotification(
            pushMessage.title,
            notificationOptions.unsafeCast<web.notifications.NotificationOptions>()
        )
    }

    private fun createNotificationActions(actions: List<PresentableActionModel>): Array<NotificationAction> {
        return actions.map {
            js("{}").unsafeCast<NotificationAction>().apply {
                action = it.id
                title = it.title
            }
        }.toTypedArray()
    }

    private fun createPushToInApp(it: PushToInApp): dynamic {
        val data = js("{}")
        data["campaignId"] = it.campaignId
        data["url"] = it.url
        data["ignoreViewedEvent"] = it.ignoreViewedEvent
        return data
    }
}