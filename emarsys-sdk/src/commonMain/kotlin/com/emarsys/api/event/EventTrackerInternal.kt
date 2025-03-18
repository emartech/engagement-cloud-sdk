package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.event.model.toSdkEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.core.collections.dequeue
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider
import com.emarsys.networking.clients.event.EventClientApi

import kotlinx.datetime.Instant

class EventTrackerInternal(
    private val eventClient: EventClientApi,
    private val eventTrackerContext: ApiContext<EventTrackerCall>,
    private val timestampProvider: Provider<Instant>,
    private val uuidProvider: Provider<String>,
    private val sdkLogger: Logger
) : EventTrackerInstance {

    override suspend fun trackEvent(event: CustomEvent) {
        eventClient.registerEvent(
            event.toSdkEvent(
                uuidProvider.provide(),
                timestampProvider.provide()
            )
        )
        //TODO handle error
        sdkLogger.debug("EventTrackerGatherer - trackEvent")
    }

    override suspend fun activate() {
        sdkLogger.debug("EventTrackerGatherer - activate")

        eventTrackerContext.calls.dequeue { call ->
            when (call) {
                is EventTrackerCall.TrackEvent -> eventClient.registerEvent(call.event)
            }
        }
    }
}