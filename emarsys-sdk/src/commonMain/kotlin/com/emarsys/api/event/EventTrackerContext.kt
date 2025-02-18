package com.emarsys.api.event

import com.emarsys.api.generic.ApiContext

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.Serializable

class EventTrackerContext(override val calls: MutableList<EventTrackerCall>) : ApiContext<EventTrackerCall>

@Serializable
sealed interface EventTrackerCall {

    @Serializable
    data class TrackEvent(val event: SdkEvent) : EventTrackerCall
}