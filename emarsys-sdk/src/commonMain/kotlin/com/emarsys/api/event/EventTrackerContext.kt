package com.emarsys.api.event

import com.emarsys.api.generic.ApiContext
import com.emarsys.networking.clients.event.model.Event

class EventTrackerContext : ApiContext<EventTrackerCall> {
    override var calls = mutableListOf<EventTrackerCall>()
}

sealed interface EventTrackerCall {
    data class TrackEvent(val event: Event) : EventTrackerCall
}