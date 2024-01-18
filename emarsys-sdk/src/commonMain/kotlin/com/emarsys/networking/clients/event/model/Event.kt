package com.emarsys.core.networking.clients.event.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val eventType: EventType,
    val eventName: String,
    val attributes: Map<String, String>? = null
)

enum class EventType {
    INTERNAL, CUSTOM
}