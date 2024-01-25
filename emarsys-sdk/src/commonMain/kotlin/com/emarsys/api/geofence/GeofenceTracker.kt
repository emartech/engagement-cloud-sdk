package com.emarsys.api.geofence

import com.emarsys.api.AppEvent
import com.emarsys.api.SdkResult
import kotlinx.coroutines.flow.Flow

class GeofenceTracker : GeofenceApi {
    override val registeredGeofences: List<Geofence>
        get() = TODO("Not yet implemented")

    override suspend fun enable(): SdkResult {
        TODO("Not yet implemented")
    }

    override suspend fun disable(): SdkResult {
        TODO("Not yet implemented")
    }

    override val events: Flow<AppEvent>
        get() = TODO("Not yet implemented")
    override val isEnabled: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun setInitialEnterTriggerEnabled(enabled: Boolean): SdkResult {
        TODO("Not yet implemented")
    }
}