package com.emarsys.networking.clients.event.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DeviceEventRequestBody(
    val dnd: Boolean = false,
    val events: List<Event>,
    val deviceEventState: JsonObject? = null
)