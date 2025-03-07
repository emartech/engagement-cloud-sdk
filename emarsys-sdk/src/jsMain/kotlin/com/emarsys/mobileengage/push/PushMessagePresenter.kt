package com.emarsys.mobileengage.push


import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.mobileengage.push.model.WebPushNotificationData
import com.emarsys.util.JsonUtil
import org.w3c.notifications.NotificationAction
import org.w3c.notifications.NotificationOptions

open class PushMessagePresenter(private val webPushNotificationPresenter: WebPushNotificationPresenterApi) :
    PushPresenter<JsPlatformData, JsPushMessage> {

    override suspend fun present(pushMessage: JsPushMessage) {
        pushMessage.displayableData?.let {
            val notificationOptions = js("{}").unsafeCast<NotificationOptions>().apply {
                body = it.body
                icon = it.iconUrlString
                badge = it.imageUrlString
            }

            pushMessage.actionableData?.actions?.let {
                notificationOptions.actions = createNotificationActions(it)
            }

            notificationOptions.data = JsonUtil.json.encodeToString<JsPushMessage>(pushMessage)

            webPushNotificationPresenter.showNotification(
                WebPushNotificationData(
                    it.title,
                    notificationOptions
                )
            )
        }
    }

    private fun createNotificationActions(actions: List<PresentableActionModel>): Array<NotificationAction> {
        return actions.map {
            js("{}").unsafeCast<NotificationAction>().apply {
                action = it.id
                title = it.title
            }
        }.toTypedArray()
    }
}