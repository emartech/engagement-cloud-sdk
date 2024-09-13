package com.emarsys.mobileengage.push.model

import com.emarsys.mobileengage.inapp.PushToInApp

data class JsNotificationOptions(
    var body: String,
    var icon: String?,
    var badge: String?,
    val actions: List<JsNotificationAction>,
    val pushToInApp: PushToInApp? = null
)