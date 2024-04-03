package com.emarsys.networking.clients.event.model

import com.emarsys.core.providers.TimestampProvider
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val type: EventType,
    val name: String,
    val attributes: Map<String, String>? = null,
    val timestamp: String = TimestampProvider().provide().toString()
) {
    companion object {
        fun createAppStart(timestamp: String): Event =
            Event(type = EventType.INTERNAL, name = "app:start", timestamp = timestamp)

        fun createSessionStart(timestamp: String): Event =
            Event(type = EventType.INTERNAL, name = "session:start", timestamp = timestamp)

        fun createSessionEnd(duration: Long, timestamp: String): Event =
            Event(
                type = EventType.INTERNAL,
                name = "session:end",
                attributes = mapOf("duration" to duration.toString()),
                timestamp = timestamp
            )
    }
}

enum class EventType {
    INTERNAL, CUSTOM
}

