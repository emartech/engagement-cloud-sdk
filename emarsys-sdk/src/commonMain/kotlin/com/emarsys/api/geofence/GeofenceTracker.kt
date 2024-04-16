package com.emarsys.api.geofence

import Activatable
import com.emarsys.api.AppEvent
import com.emarsys.api.generic.GenericApi
import com.emarsys.api.geofence.model.Geofence
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface GeofenceTrackerInstance : GeofenceTrackerInternalApi, Activatable

class GeofenceTracker<Logging : GeofenceTrackerInstance, Gatherer : GeofenceTrackerInstance, Internal : GeofenceTrackerInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(
    loggingApi, gathererApi, internalApi, sdkContext
), GeofenceTrackerApi {
    override val registeredGeofences: List<Geofence>
        get() = activeInstance<GeofenceTrackerInstance>().registeredGeofences

    override val events: Flow<AppEvent>
        get() = activeInstance<GeofenceTrackerInstance>().events

    override val isEnabled: Boolean
        get() = activeInstance<GeofenceTrackerInstance>().isEnabled

    override suspend fun enable(): Result<Unit> {
        return runCatching {
            withContext(sdkContext.sdkDispatcher) {
                activeInstance<GeofenceTrackerInstance>().enable()
            }
        }
    }

    override suspend fun disable(): Result<Unit> {
        return runCatching {
            withContext(sdkContext.sdkDispatcher) {
                activeInstance<GeofenceTrackerInstance>().disable()
            }
        }
    }

    override suspend fun setInitialEnterTriggerEnabled(enabled: Boolean): Result<Unit> {
        return runCatching {
            withContext(sdkContext.sdkDispatcher) {
                activeInstance<GeofenceTrackerInstance>().setInitialEnterTriggerEnabled(enabled)
            }
        }
    }
}