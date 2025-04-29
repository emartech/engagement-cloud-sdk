package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence
import kotlin.js.Promise

@JsExport
interface JSGeofenceApi {
    val registeredGeofences: List<Geofence>

    val isEnabled: Boolean

    fun enable(): Promise<Unit>

    fun disable(): Promise<Unit>

    fun setInitialEnterTriggerEnabled(enabled: Boolean): Promise<Unit>
}