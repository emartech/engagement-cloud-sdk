package com.emarsys.api.geofence

import com.emarsys.api.AppEvent
import com.emarsys.api.geofence.model.Geofence
import kotlinx.coroutines.flow.Flow

interface GeofenceTrackerApi {
    val registeredGeofences: List<Geofence>

    val events: Flow<AppEvent>

    val isEnabled: Boolean

    suspend fun enable(): Result<Unit>

    suspend fun disable(): Result<Unit>

    suspend fun setInitialEnterTriggerEnabled(enabled: Boolean): Result<Unit>
}