package com.emarsys.api.geofence

import com.emarsys.api.AppEvent
import com.emarsys.api.geofence.model.Geofence
import kotlinx.coroutines.flow.Flow

interface GeofenceTrackerInternalApi {

    val registeredGeofences: List<Geofence>

    val events: Flow<AppEvent>

    val isEnabled: Boolean

    suspend fun enable()

    suspend fun disable()

    suspend fun setInitialEnterTriggerEnabled(enabled: Boolean)
}