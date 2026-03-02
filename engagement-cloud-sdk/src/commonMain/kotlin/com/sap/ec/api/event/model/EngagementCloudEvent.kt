package com.sap.ec.api.event.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalSerializationApi::class, ExperimentalJsExport::class)
@JsExport
@Serializable
@JsonClassDiscriminator("fullClassName")
sealed interface EngagementCloudEvent {
    val type: EventType
}