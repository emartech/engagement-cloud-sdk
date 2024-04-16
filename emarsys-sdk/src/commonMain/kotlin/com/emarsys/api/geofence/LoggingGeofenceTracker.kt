package com.emarsys.api.geofence

import com.emarsys.api.AppEvent
import com.emarsys.api.geofence.model.Geofence
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class LoggingGeofenceTracker(private val logger: Logger) : GeofenceTrackerInstance {
    override val registeredGeofences: List<Geofence>
        get() {
            val entry = LogEntry.createMethodNotAllowed<LoggingGeofenceTracker>(
                this, this::registeredGeofences.name
            )
            logger.debug(entry)
            return listOf()
        }

    override val events: Flow<AppEvent>
        get() {
            val entry = LogEntry.createMethodNotAllowed<LoggingGeofenceTracker>(
                this, this::events.name
            )
            logger.debug(entry)
            return emptyFlow()
        }

    override val isEnabled: Boolean
        get() {
            val entry = LogEntry.createMethodNotAllowed<LoggingGeofenceTracker>(
                this, this::isEnabled.name
            )
            logger.debug(entry)
            return false
        }

    override suspend fun enable() {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::enable.name
        )
        logger.debug(entry)
    }

    override suspend fun disable() {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::disable.name
        )
        logger.debug(entry)
    }

    override suspend fun setInitialEnterTriggerEnabled(enabled: Boolean) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::setInitialEnterTriggerEnabled.name, mapOf(
                "enabled" to enabled,
            )
        )
        logger.debug(entry)
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.log(entry, LogLevel.Debug)
    }
}