package com.sap.ec.mobileengage.push.model

import kotlinx.serialization.Serializable

@Serializable
data class JsNotificationClickedData(
    val actionId: String,
    val jsPushMessage: JsPushMessage
)
