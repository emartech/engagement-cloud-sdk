package com.emarsys.api.event

import com.emarsys.api.event.EventTrackerCall.TrackEvent
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.event.model.toSdkEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider

import kotlinx.datetime.Instant

class EventTrackerGatherer(
    private val context: ApiContext<EventTrackerCall>,
    private val timestampProvider: Provider<Instant>,
    private val uuidProvider: Provider<String>,
    private val sdkLogger: Logger
) : EventTrackerInstance {
    override suspend fun trackEvent(event: CustomEvent) {
        context.calls.add(
            TrackEvent(
                event.toSdkEvent(
                    uuidProvider.provide(),
                    timestampProvider.provide(),
                )
            )
        )
        sdkLogger.debug("EventTrackerGatherer - trackEvent")
    }

    override suspend fun activate() {
    }
}