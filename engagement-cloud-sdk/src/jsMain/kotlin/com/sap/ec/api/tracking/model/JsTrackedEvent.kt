package com.sap.ec.api.tracking.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalJsExport::class, ExperimentalSerializationApi::class)
@JsExport
@JsName("EngagementCloudTrackedEvent")
@Serializable
@JsonClassDiscriminator("eventType")
sealed interface JsTrackedEvent {
    val type: String
}