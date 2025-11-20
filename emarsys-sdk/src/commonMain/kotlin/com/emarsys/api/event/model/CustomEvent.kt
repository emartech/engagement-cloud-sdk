package com.emarsys.api.event.model

import com.emarsys.event.SdkEvent
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents a custom event that can be tracked using the SDK.
 *
 * @property name The name of the custom event.
 * @property attributes A map of string key-value pairs representing additional attributes for the event.
 */
data class CustomEvent(
    val name: String,
    val attributes: Map<String, String>? = null
)

@OptIn(ExperimentalTime::class)
fun CustomEvent.toSdkEvent(uuid: String, timestamp: Instant): SdkEvent = SdkEvent.External.Custom(
    id = uuid,
    name = name,
    attributes = attributes?.let { attributes ->
        buildJsonObject {
            attributes.forEach { (key, value) ->
                put(
                    key,
                    JsonPrimitive(value)
                )
            }
        }
    },
    timestamp = timestamp
)