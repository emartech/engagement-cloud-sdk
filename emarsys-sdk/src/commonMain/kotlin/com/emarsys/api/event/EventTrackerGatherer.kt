package com.emarsys.api.event

import com.emarsys.api.event.EventTrackerCall.TrackEvent
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.core.providers.Provider
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import kotlinx.datetime.Instant

class EventTrackerGatherer(
    private val context: ApiContext<EventTrackerCall>,
    private val timestampProvider: Provider<Instant>
) : EventTrackerInstance {
    override suspend fun trackEvent(event: CustomEvent) {
        val deviceEvent = Event(
            EventType.CUSTOM,
            event.name,
            event.attributes,
            timestampProvider.provide().toString()
        )

        context.calls.add(TrackEvent(deviceEvent))
    }

    override suspend fun activate() {
    }
}