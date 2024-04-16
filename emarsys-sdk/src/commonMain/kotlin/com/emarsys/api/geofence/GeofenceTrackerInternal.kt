package com.emarsys.api.geofence

import com.emarsys.api.AppEvent
import com.emarsys.api.geofence.model.Geofence
import kotlinx.coroutines.flow.Flow

class GeofenceTrackerInternal: GeofenceTrackerInstance {
    override val registeredGeofences: List<Geofence>
        get() = TODO("Not yet implemented")
    override val events: Flow<AppEvent>
        get() = TODO("Not yet implemented")
    override val isEnabled: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun enable() {
        TODO("Not yet implemented")
    }

    override suspend fun disable() {
        TODO("Not yet implemented")
    }

    override suspend fun setInitialEnterTriggerEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun activate() {
        TODO("Not yet implemented")
    }
}