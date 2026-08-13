package com.sap.ec.api.event

import com.sap.ec.api.event.EventTrackerCall.TrackEvent
import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class EventTrackerGatherer(
    private val threadSafePersistentStore: ThreadSafePersistentStoreApi<EventTrackerCall>,
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger
) : EventTrackerInstance {
    override suspend fun trackEvent(trackedEvent: TrackedEvent) {
        val event = trackedEvent.toSdkEvent(
            uuidProvider.provide(),
            timestampProvider.provide(),
        ).getOrThrow()

        threadSafePersistentStore.add(TrackEvent(event))
        sdkLogger.debug("EventTrackerGatherer - trackEvent")
    }

    override suspend fun activate() {
    }
}