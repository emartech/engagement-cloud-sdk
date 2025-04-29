package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

class JSGeofence(
    private val geofenceTracker: GeofenceTrackerApi,
    private val applicationScope: CoroutineScope
) : JSGeofenceApi {
    override val registeredGeofences: List<Geofence> = geofenceTracker.registeredGeofences
    override val isEnabled: Boolean = geofenceTracker.isEnabled

    override fun enable(): Promise<Unit> {
        return applicationScope.promise {
            geofenceTracker.enable().getOrThrow()
        }
    }

    override fun disable(): Promise<Unit> {
        return applicationScope.promise {
            geofenceTracker.disable().getOrThrow()
        }
    }

    override fun setInitialEnterTriggerEnabled(enabled: Boolean): Promise<Unit> {
        return applicationScope.promise {
            geofenceTracker.setInitialEnterTriggerEnabled(enabled).getOrThrow()
        }
    }
}