package com.emarsys.api.event

import com.emarsys.api.event.EventTrackerCall.TrackEvent
import com.emarsys.api.event.model.TrackedEvent
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class EventTrackerGatherer(
    private val context: EventTrackerContextApi,
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger
) : EventTrackerInstance {
    override suspend fun trackEvent(trackedEvent: TrackedEvent) {
        context.calls.add(
            TrackEvent(
                trackedEvent.toSdkEvent(
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