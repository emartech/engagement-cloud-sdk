package com.emarsys.api.event

import com.emarsys.api.SdkResult
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.core.exceptions.MethodNotAllowedException
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger

class LoggingEventTracker(private val logger: Logger) : EventTrackerInstance {
    override suspend fun trackEvent(event: CustomEvent): SdkResult {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::trackEvent.name, mapOf(
                "event" to event,
            )
        )
        logger.log(entry, LogLevel.Debug)
        return SdkResult.Failure(MethodNotAllowedException(entry))
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.log(entry, LogLevel.Debug)
    }
}