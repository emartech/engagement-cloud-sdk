package com.emarsys.api.event

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.Serializable

internal class EventTrackerContext(
    override val calls: MutableList<EventTrackerCall>
) : EventTrackerContextApi

@Serializable
sealed interface EventTrackerCall {

    @Serializable
    data class TrackEvent(val event: SdkEvent) : EventTrackerCall
}