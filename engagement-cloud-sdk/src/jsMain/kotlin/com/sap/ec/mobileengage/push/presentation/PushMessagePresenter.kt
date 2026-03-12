package com.sap.ec.mobileengage.push.presentation

import com.sap.ec.InternalSdkApi
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.push.PushPresenter
import com.sap.ec.mobileengage.push.WebPushNotificationPresenterApi
import com.sap.ec.mobileengage.push.model.JsPlatformData
import com.sap.ec.mobileengage.push.model.JsPushMessage
import com.sap.ec.mobileengage.push.model.WebPushNotificationData
import com.sap.ec.util.JsonUtil
import org.w3c.notifications.NotificationAction
import org.w3c.notifications.NotificationOptions


@InternalSdkApi
open class PushMessagePresenter(private val webPushNotificationPresenter: WebPushNotificationPresenterApi) :
    PushPresenter<JsPlatformData, JsPushMessage> {

    override suspend fun present(pushMessage: JsPushMessage) {
        println("Presenting push message with id: ${pushMessage}")
        pushMessage.displayableData?.let {
            val notificationOptions = js("{}").unsafeCast<NotificationOptions>().apply {
                body = it.body
                icon = it.iconUrlString
                image = it.imageUrlString
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
        }.toTypedArray().also {
            println(
                "Presenting push message with id: ${
                    it.joinToString { action ->
                        JSON.stringify(
                            action
                        )
                    }
                }"
            )
        }
    }
}