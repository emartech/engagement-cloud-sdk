package com.emarsys.api.geofence

import com.emarsys.api.AppEvent
import com.emarsys.api.SdkResult
import kotlinx.coroutines.flow.Flow

interface GeofenceApi {
    val registeredGeofences: List<Geofence>
    suspend fun enable(): SdkResult

    suspend fun disable(): SdkResult

    val events: Flow<AppEvent>

    val isEnabled: Boolean

    suspend fun setInitialEnterTriggerEnabled(enabled: Boolean): SdkResult
}