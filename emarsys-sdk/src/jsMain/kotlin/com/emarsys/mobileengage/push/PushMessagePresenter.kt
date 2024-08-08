package com.emarsys.mobileengage.push


import com.emarsys.mobileengage.push.model.JsNotificationAction
import com.emarsys.mobileengage.push.model.JsNotificationOptions
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage

open class PushMessagePresenter(
    private val serviceWorkerRegistrationWrapper: ServiceWorkerRegistrationWrapper
) : PushPresenter<JsPlatformData, JsPushMessage> {

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
            } ?: emptyList()
        )

        serviceWorkerRegistrationWrapper.showNotification(
            pushMessage.title,
            jsNotificationOptions
        )
    }

}