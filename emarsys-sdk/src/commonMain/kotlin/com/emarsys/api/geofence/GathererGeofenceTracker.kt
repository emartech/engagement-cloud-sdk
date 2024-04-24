package com.emarsys.api.geofence

import com.emarsys.api.AppEvent
import com.emarsys.api.geofence.model.Geofence
import kotlinx.coroutines.flow.Flow

class GathererGeofenceTracker(
    private val geofenceTrackerContext: GeofenceContext,
    override val events: Flow<AppEvent>
) : GeofenceTrackerInstance {
    override val registeredGeofences: List<Geofence>
        get() = geofenceTrackerContext.registeredGeofences

    override val isEnabled: Boolean
        get() = geofenceTrackerContext.isGeofenceTrackerEnabled

    override suspend fun enable() {
        geofenceTrackerContext.calls.add(GeofenceTrackerCall.Enable())
    }

    override suspend fun disable() {
        geofenceTrackerContext.calls.add(GeofenceTrackerCall.Disable())
    }

    override suspend fun setInitialEnterTriggerEnabled(enabled: Boolean) {
        geofenceTrackerContext.calls.add(GeofenceTrackerCall.SetInitialEnterTriggerEnabled(enabled))
    }

    override suspend fun activate() {}
}