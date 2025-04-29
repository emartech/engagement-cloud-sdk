package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence
import com.emarsys.di.SdkKoinIsolationContext.koin

class IosGeofence: IosGeofenceApi {
    override val registeredGeofences: List<Geofence> = koin.get<GeofenceTrackerApi>().registeredGeofences
    override val isEnabled: Boolean = koin.get<GeofenceTrackerApi>().isEnabled

    override suspend fun enable() {
        koin.get<GeofenceTrackerApi>().enable().getOrThrow()
    }

    override suspend fun disable() {
        koin.get<GeofenceTrackerApi>().disable().getOrThrow()
    }

    override suspend fun setInitialEnterTriggerEnabled(enabled: Boolean) {
        koin.get<GeofenceTrackerApi>().setInitialEnterTriggerEnabled(enabled).getOrThrow()
    }
}