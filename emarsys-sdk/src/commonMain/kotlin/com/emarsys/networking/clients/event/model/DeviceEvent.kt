package com.emarsys.networking.clients.event.model

import com.emarsys.SdkConstants
import com.emarsys.event.SdkEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@Serializable
@OptIn(ExperimentalTime::class)
data class DeviceEvent(
    val type: String,
    val name: String,
    val timestamp: Instant,
    val attributes: Map<String, String>? = null,
    val trackingInfo: String? = null,
    val reporting: String? = null,
)

@OptIn(ExperimentalTime::class)
fun SdkEvent.DeviceEvent.toDeviceEvent(
    platformCategory: String = SdkConstants.MOBILE_PLATFORM_CATEGORY,
    pageLocation: String
): DeviceEvent {
    val attributes =
        (this.attributes?.mapValues { it.value.jsonPrimitive.content }?.toMutableMap()
            ?: mutableMapOf())
            .also {
                if (platformCategory == SdkConstants.WEB_PLATFORM_CATEGORY) {
                    it.put(SdkConstants.PAGE_LOCATION_ATTRIBUTE_KEY, pageLocation)
                }
            }

    return DeviceEvent(
        type = this.type,
        name = this.name,
        timestamp = this.timestamp,
        attributes = attributes,
        trackingInfo = if (this is SdkEvent.Internal.Reporting) this.trackingInfo else null,
        reporting = if (this is SdkEvent.Internal.Reporting) this.reporting else null
    )
}