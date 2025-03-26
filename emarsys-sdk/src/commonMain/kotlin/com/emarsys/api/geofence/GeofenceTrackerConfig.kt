package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence
import com.emarsys.di.SdkComponent

internal object GeofenceTrackerConfig: GeofenceTrackerConfigApi, SdkComponent {
    override var isGeofenceTrackerEnabled: Boolean = false
    override val registeredGeofences: MutableList<Geofence> = mutableListOf()
}