package com.emarsys.mobileengage.push.model

data class JsNotificationOptions(
    var body: String,
    var icon: String?,
    var badge: String?,
    val actions: List<JsNotificationAction>
)