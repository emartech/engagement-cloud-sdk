package com.emarsys.api.event

import com.emarsys.api.event.EventTrackerCall.TrackEvent
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.event.model.toSdkEvent
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi

internal class EventTrackerGatherer(
    private val context: EventTrackerContextApi,
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi,
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