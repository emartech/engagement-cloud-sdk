package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence

internal interface GeofenceTrackerConfigApi {
    var isGeofenceTrackerEnabled: Boolean
    val registeredGeofences: MutableList<Geofence>
}