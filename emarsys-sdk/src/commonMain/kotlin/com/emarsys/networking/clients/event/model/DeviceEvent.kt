package com.emarsys.networking.clients.event.model

import kotlinx.serialization.Serializable
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