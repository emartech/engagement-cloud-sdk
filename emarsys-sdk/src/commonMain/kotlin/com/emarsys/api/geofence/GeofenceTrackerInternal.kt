package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence

class GeofenceTrackerInternal : GeofenceTrackerInstance {
    override val registeredGeofences: List<Geofence>
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