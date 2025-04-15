package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.event.model.toSdkEvent
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.collections.dequeue
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi

internal class EventTrackerInternal(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val eventTrackerContext: EventTrackerContextApi,
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger
) : EventTrackerInstance {

    override suspend fun trackEvent(event: CustomEvent) {
        sdkEventDistributor.registerEvent(
            event.toSdkEvent(
                uuidProvider.provide(),
                timestampProvider.provide()
            )
        )?.await()
        sdkLogger.debug("EventTrackerInternal - trackEvent")
    }

    override suspend fun activate() {
        sdkLogger.debug("EventTrackerInternal - activate")

        eventTrackerContext.calls.dequeue { call ->
            when (call) {
                is EventTrackerCall.TrackEvent -> sdkEventDistributor.registerEvent(call.event)
            }
        }
    }
}