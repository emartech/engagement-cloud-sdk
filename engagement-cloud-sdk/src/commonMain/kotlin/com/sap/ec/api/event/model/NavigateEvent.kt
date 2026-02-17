package com.sap.ec.api.event.model

import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class NavigateEvent(
    val location: String
) : TrackedEvent {
    @OptIn(ExperimentalTime::class)
    override fun toSdkEvent(
        uuid: String,
        timestamp: Instant
    ): SdkEvent {
        return SdkEvent.External.NavigateEvent(
            id = uuid,
            timestamp = timestamp,
            location = location
        )
    }
}