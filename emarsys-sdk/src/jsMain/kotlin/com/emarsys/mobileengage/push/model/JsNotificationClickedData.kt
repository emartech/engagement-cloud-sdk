package com.emarsys.mobileengage.push.model

import kotlinx.serialization.Serializable

@Serializable
data class JsNotificationClickedData(
    val actionId: String,
    val jsPushMessage: JsPushMessage
)
