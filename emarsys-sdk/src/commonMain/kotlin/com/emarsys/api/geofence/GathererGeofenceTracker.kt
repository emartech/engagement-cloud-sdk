package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence

internal class GathererGeofenceTracker(
    private val geofenceTrackerContext: GeofenceContextApi,
    val geofenceTrackerConfig: GeofenceTrackerConfigApi
) : GeofenceTrackerInstance {
    override val registeredGeofences: List<Geofence>
        get() = geofenceTrackerConfig.registeredGeofences

    override val isEnabled: Boolean
        get() = geofenceTrackerConfig.isGeofenceTrackerEnabled

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