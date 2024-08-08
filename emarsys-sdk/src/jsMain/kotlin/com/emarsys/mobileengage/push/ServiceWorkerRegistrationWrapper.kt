package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.push.model.JsNotificationOptions
import com.emarsys.self
import js.promise.await
import org.w3c.notifications.NotificationAction
import web.notifications.NotificationOptions

open class ServiceWorkerRegistrationWrapper {

    @OptIn(ExperimentalJsExport::class, ExperimentalJsCollectionsApi::class)
    open suspend fun showNotification(
        notificationTitle: String,
        jsNotificationOptions: JsNotificationOptions
    ) {
        val notificationOptions = js("{}").unsafeCast<NotificationOptions>().apply {
            body = jsNotificationOptions.body
            icon = jsNotificationOptions.icon
            badge = jsNotificationOptions.badge
        }

        notificationOptions.asDynamic().actions = jsNotificationOptions.actions.map {
            js("{}").unsafeCast<NotificationAction>().apply {
                action = it.action
                title = it.title
            }
        }.asJsReadonlyArrayView()

        self.registration.showNotification(notificationTitle, notificationOptions).await()
    }
}