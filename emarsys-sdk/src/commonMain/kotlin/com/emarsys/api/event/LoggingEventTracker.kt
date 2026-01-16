package com.emarsys.api.event

import com.emarsys.api.event.model.TrackedEvent
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class LoggingEventTracker(private val logger: Logger) : EventTrackerInstance {
    override suspend fun trackEvent(trackedEvent: TrackedEvent) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::trackEvent.name, buildJsonObject {
                put("event", JsonPrimitive(trackedEvent.toString()))
            }
        )
        logger.debug(entry)
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.debug(entry)
    }
}