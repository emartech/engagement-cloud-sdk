package com.emarsys.api.event.model

import com.emarsys.event.SdkEvent
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

data class CustomEvent(
    val name: String,
    val attributes: Map<String, String>? = null
)

fun CustomEvent.toSdkEvent(uuid: String, timestamp: Instant): SdkEvent = SdkEvent.External.Custom(
    id  = uuid,
    name = name,
    attributes = buildJsonObject {
        attributes?.forEach { (key, value) ->
            put(
                key,
                JsonPrimitive(value)
            )
        }
    },
    timestamp = timestamp
)