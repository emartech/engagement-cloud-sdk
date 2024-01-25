package com.emarsys.api.event

import com.emarsys.api.SdkResult
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType

class EventTrackerGatherer(private val context: ApiContext<EventTrackerCall>) : EventTrackerInstance {
    override suspend fun trackEvent(event: CustomEvent): SdkResult {
        context.calls.add(EventTrackerCall.TrackEvent(Event(EventType.CUSTOM, event.name, event.attributes)))
        return SdkResult.Success(Unit)
    }

    override suspend fun activate() {
    }
}