package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.core.collections.dequeue
import com.emarsys.core.providers.Provider
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import kotlinx.datetime.Instant

class EventTrackerInternal(
    private val eventClient: EventClientApi,
    private val eventTrackerContext: ApiContext<EventTrackerCall>,
    private val timestampProvider: Provider<Instant>
) : EventTrackerInstance {

    override suspend fun trackEvent(event: CustomEvent) {
        val deviceEvent = Event(
            EventType.CUSTOM,
            event.name,
            event.attributes,
            timestampProvider.provide().toString()
        )
        eventClient.registerEvent(deviceEvent)
        //TODO handle error
    }

    override suspend fun activate() {
        eventTrackerContext.calls.dequeue { call ->
            when (call) {
                is EventTrackerCall.TrackEvent -> eventClient.registerEvent(call.event)
            }
        }
    }
}