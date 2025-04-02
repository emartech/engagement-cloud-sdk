package com.emarsys.api.geofence

import kotlinx.serialization.Serializable

internal class GeofenceTrackerContext(override val calls: MutableList<GeofenceTrackerCall>) : GeofenceContextApi

@Serializable
sealed interface GeofenceTrackerCall {

    @Serializable
    class Enable : GeofenceTrackerCall {
        override fun equals(other: Any?): Boolean {
            return other is Enable
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    @Serializable
    class Disable : GeofenceTrackerCall {
        override fun equals(other: Any?): Boolean {
            return other is Disable
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    @Serializable
    data class SetInitialEnterTriggerEnabled(val enabled: Boolean) : GeofenceTrackerCall

}