package com.sap.ec.mobileengage.push.model

import com.sap.ec.InternalSdkApi
import kotlinx.serialization.Serializable

@Serializable
internal data class JsNotificationClickedData(
    val actionId: String,
    val jsPushMessage: JsPushMessage
)
