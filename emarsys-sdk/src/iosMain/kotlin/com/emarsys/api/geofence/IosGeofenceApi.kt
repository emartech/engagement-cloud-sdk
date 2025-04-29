package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence
import kotlin.coroutines.cancellation.CancellationException

interface IosGeofenceApi {
    val registeredGeofences: List<Geofence>
    val isEnabled: Boolean

    @Throws(CancellationException::class)
    suspend fun enable()

    @Throws(CancellationException::class)
    suspend fun disable()

    @Throws(CancellationException::class)
    suspend fun setInitialEnterTriggerEnabled(enabled: Boolean)
}