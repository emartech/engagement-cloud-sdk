package com.sap.ec.mobileengage.push.model

import com.sap.ec.InternalSdkApi
import kotlinx.serialization.Serializable

@InternalSdkApi
@Serializable
data class JsNotificationClickedData(
    val actionId: String,
    val jsPushMessage: JsPushMessage
)
