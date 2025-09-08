package com.emarsys.networking.clients.event.model

import com.emarsys.event.SdkEvent
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class DeviceEvent(
    val type: String,
    val name: String,
    val timestamp: Instant,
    val attributes: Map<String, String>? = null,
    val trackingInfo: String? = null,
    val reporting: String? = null,
)

fun SdkEvent.DeviceEvent.toDeviceEvent(): DeviceEvent = DeviceEvent(
    type = this.type,
    name = this.name,
    timestamp = this.timestamp,
    attributes = this.attributes?.mapValues { it.value.jsonPrimitive.content },
    trackingInfo = if (this is SdkEvent.Internal.Reporting) this.trackingInfo else null,
    reporting = if (this is SdkEvent.Internal.Reporting) this.reporting else null
)