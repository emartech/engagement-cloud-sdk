package com.emarsys.networking.clients.event.model

import com.emarsys.core.providers.TimestampProvider
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val type: EventType,
    val name: String,
    val attributes: Map<String, String>? = null,
    val timestamp: String = TimestampProvider().provide().toString()
) {
    companion object {
        fun createAppStart(timestamp: Instant): Event =
            Event(type = EventType.INTERNAL, name = "app:start", timestamp = timestamp.toString())

        fun createSessionStart(timestamp: Instant): Event =
            Event(
                type = EventType.INTERNAL,
                name = "session:start",
                timestamp = timestamp.toString()
            )

        fun createSessionEnd(duration: Long, timestamp: Instant): Event =
            Event(
                type = EventType.INTERNAL,
                name = "session:end",
                attributes = mapOf("duration" to duration.toString()),
                timestamp = timestamp.toString()
            )
    }
}

enum class EventType {
    INTERNAL, CUSTOM
}

