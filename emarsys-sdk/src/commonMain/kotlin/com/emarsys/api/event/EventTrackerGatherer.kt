package com.emarsys.api.event

import com.emarsys.api.generic.ApiContext
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType

class EventTrackerGatherer(private val context: ApiContext<EventTrackerCall>) : EventTrackerInstance {
    override suspend fun trackEvent(event: CustomEvent) {
        context.calls.add(EventTrackerCall.TrackEvent(Event(EventType.CUSTOM, event.name, event.attributes)))

    }

    override suspend fun activate() {
    }
}