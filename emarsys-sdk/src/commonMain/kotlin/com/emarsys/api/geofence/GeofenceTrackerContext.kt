package com.emarsys.api.geofence

import com.emarsys.api.generic.ApiContext
import com.emarsys.api.geofence.model.Geofence
import kotlinx.serialization.Serializable

interface GeofenceContext: ApiContext<GeofenceTrackerCall> {
    override val calls: MutableList<GeofenceTrackerCall>
    var isGeofenceTrackerEnabled: Boolean
    val registeredGeofences: MutableList<Geofence>
}
class GeofenceTrackerContext(override val calls: MutableList<GeofenceTrackerCall>): GeofenceContext {
    override var isGeofenceTrackerEnabled: Boolean = false
    override val registeredGeofences: MutableList<Geofence> = mutableListOf()
}

@Serializable
sealed interface GeofenceTrackerCall {

    @Serializable
    class Enable: GeofenceTrackerCall {
        override fun equals(other: Any?): Boolean {
            return other is Enable
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    @Serializable
    class Disable: GeofenceTrackerCall {
        override fun equals(other: Any?): Boolean {
            return other is Disable
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    @Serializable
    data class SetInitialEnterTriggerEnabled(val enabled: Boolean): GeofenceTrackerCall

}