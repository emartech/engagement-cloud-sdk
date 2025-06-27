package com.emarsys.networking.clients.event.model

import com.emarsys.event.SdkEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DeviceEventRequestBody(
    val dnd: Boolean = false,
    val events: List<SdkEvent>,
    val deviceEventState: JsonObject? = null
)