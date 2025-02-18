package com.emarsys.api.event.model

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

data class CustomEvent(
    val name: String,
    val attributes: Map<String, String>? = null
)

//TODO are there any better way?
fun CustomEvent.toSdkEvent(timestamp: Instant): SdkEvent {
    val deviceEvent = SdkEvent.External.Incoming(
        name,
        buildJsonObject {
            attributes?.forEach { (key, value) ->
                put(
                    key,
                    JsonPrimitive(value)
                )
            }
        },
        timestamp
    )
    return deviceEvent
}