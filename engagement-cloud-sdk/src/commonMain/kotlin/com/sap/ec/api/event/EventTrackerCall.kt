package com.sap.ec.api.event

import com.sap.ec.event.SdkEvent
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface EventTrackerCall {

    @Serializable
    data class TrackEvent(val event: SdkEvent) : EventTrackerCall
}