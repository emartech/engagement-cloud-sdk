package com.emarsys.networking.clients.event.model

import com.emarsys.core.providers.TimestampProvider
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val type: EventType,
    val name: String,
    val attributes: Map<String, String>? = null,
    val timestamp: String = TimestampProvider().provide().toString()
)

enum class EventType {
    INTERNAL, CUSTOM
}