package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence

internal object GeofenceTrackerConfig: GeofenceTrackerConfigApi {
    override var isGeofenceTrackerEnabled: Boolean = false
    override val registeredGeofences: MutableList<Geofence> = mutableListOf()
}