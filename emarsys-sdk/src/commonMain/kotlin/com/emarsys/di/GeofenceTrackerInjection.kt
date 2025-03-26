package com.emarsys.di

import com.emarsys.api.geofence.GathererGeofenceTracker
import com.emarsys.api.geofence.GeofenceContextApi
import com.emarsys.api.geofence.GeofenceTracker
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.geofence.GeofenceTrackerCall
import com.emarsys.api.geofence.GeofenceTrackerConfig
import com.emarsys.api.geofence.GeofenceTrackerConfigApi
import com.emarsys.api.geofence.GeofenceTrackerContext
import com.emarsys.api.geofence.GeofenceTrackerInstance
import com.emarsys.api.geofence.GeofenceTrackerInternal
import com.emarsys.api.geofence.LoggingGeofenceTracker
import com.emarsys.api.geofence.model.Geofence
import com.emarsys.core.collections.PersistentList
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object GeofenceTrackerInjection {
    const val REGISTERED_GEOFENCES = "registeredGeofences"
    val geofenceTrackerModules = module {
        single<MutableList<GeofenceTrackerCall>>(named(PersistentListTypes.GeofenceTrackerCall)) {
            PersistentList(
                id = PersistentListIds.GEOFENCE_TRACKER_CONTEXT_PERSISTENT_ID,
                storage = get(),
                elementSerializer = GeofenceTrackerCall.serializer(),
                elements = listOf()
            )
        }
        single<MutableList<Geofence>>(named(REGISTERED_GEOFENCES)) { mutableListOf() }
        single<GeofenceContextApi> { GeofenceTrackerContext(
            calls = get(named(PersistentListTypes.GeofenceTrackerCall))
        ) }
        single<GeofenceTrackerConfigApi> { GeofenceTrackerConfig }
        single<GeofenceTrackerInstance>(named(InstanceType.Logging)) {
            LoggingGeofenceTracker(
                logger = get { parametersOf(LoggingGeofenceTracker::class.simpleName) },
                sdkContext = get()
            )
        }
        single<GeofenceTrackerInstance>(named(InstanceType.Gatherer)) {
            GathererGeofenceTracker(
                geofenceTrackerContext = get(),
                geofenceTrackerConfig = get()
            )
        }
        single<GeofenceTrackerInstance>(named(InstanceType.Internal)) { GeofenceTrackerInternal() }
        single<GeofenceTrackerApi> {
            GeofenceTracker(
                loggingApi = get(named(InstanceType.Logging)),
                gathererApi = get(named(InstanceType.Gatherer)),
                internalApi = get(named(InstanceType.Internal)),
                sdkContext = get()
            )
        }
    }
}