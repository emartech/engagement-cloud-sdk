package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence

interface GeofenceTrackerApi {
    val registeredGeofences: List<Geofence>

    val isEnabled: Boolean

    suspend fun enable(): Result<Unit>

    suspend fun disable(): Result<Unit>

    suspend fun setInitialEnterTriggerEnabled(enabled: Boolean): Result<Unit>
}