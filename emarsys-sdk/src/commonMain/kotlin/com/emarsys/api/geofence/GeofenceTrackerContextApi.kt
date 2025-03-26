package com.emarsys.api.geofence

internal interface GeofenceContextApi {
    val calls: MutableList<GeofenceTrackerCall>
}