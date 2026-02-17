package com.sap.ec.api.event

import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.collections.dequeue
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class EventTrackerInternal(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val eventTrackerContext: EventTrackerContextApi,
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi,
    private val sdkLogger: Logger
) : EventTrackerInstance {

    override suspend fun trackEvent(trackedEvent: TrackedEvent) {
        sdkEventDistributor.registerEvent(
            trackedEvent.toSdkEvent(
                uuidProvider.provide(),
                timestampProvider.provide()
            )
        ).await<SdkEvent.Internal.Sdk.Answer.Response<Response>>()
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