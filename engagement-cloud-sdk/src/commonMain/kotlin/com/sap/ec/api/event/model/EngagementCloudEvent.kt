package com.sap.ec.api.event.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalSerializationApi::class, ExperimentalJsExport::class)
@Serializable
@JsonClassDiscriminator("fullClassName")
sealed class EngagementCloudEvent {
    abstract val type: EventType
}