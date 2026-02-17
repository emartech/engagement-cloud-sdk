package com.sap.ec.mobileengage.push


import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.push.model.JsPlatformData
import com.sap.ec.mobileengage.push.model.JsPushMessage
import com.sap.ec.mobileengage.push.model.WebPushNotificationData
import com.sap.ec.util.JsonUtil
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
                tag = pushMessage.trackingInfo
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