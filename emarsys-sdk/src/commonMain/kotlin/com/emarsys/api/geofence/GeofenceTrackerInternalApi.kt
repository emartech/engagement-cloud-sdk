package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence

interface GeofenceTrackerInternalApi {

    val registeredGeofences: List<Geofence>

    val isEnabled: Boolean

    suspend fun enable()

    suspend fun disable()

    suspend fun setInitialEnterTriggerEnabled(enabled: Boolean)
}