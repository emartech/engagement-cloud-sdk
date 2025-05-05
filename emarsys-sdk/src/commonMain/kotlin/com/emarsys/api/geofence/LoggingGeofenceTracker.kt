package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class LoggingGeofenceTracker(
    private val sdkContext: SdkContextApi,
    private val logger: Logger
) : GeofenceTrackerInstance {
    override val registeredGeofences: List<Geofence>
        get() {
            val entry = LogEntry.createMethodNotAllowed<LoggingGeofenceTracker>(
                this, this::registeredGeofences.name
            )
            CoroutineScope(sdkContext.sdkDispatcher).launch {
                logger.debug(entry)
            }
            return listOf()
        }

    override val isEnabled: Boolean
        get() {
            val entry = LogEntry.createMethodNotAllowed<LoggingGeofenceTracker>(
                this, this::isEnabled.name
            )
            CoroutineScope(sdkContext.sdkDispatcher).launch {
                logger.debug(entry)
            }
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
            this, this::setInitialEnterTriggerEnabled.name, buildJsonObject {
                put("enabled", JsonPrimitive(enabled))
            }
        )
        logger.debug(entry)
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.debug(entry)
    }
}