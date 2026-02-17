package com.sap.ec.mobileengage.push.model

import org.w3c.notifications.NotificationOptions


@OptIn(ExperimentalJsExport::class)
@JsExport
data class WebPushNotificationData(
    val title: String,
    val options: NotificationOptions
)
