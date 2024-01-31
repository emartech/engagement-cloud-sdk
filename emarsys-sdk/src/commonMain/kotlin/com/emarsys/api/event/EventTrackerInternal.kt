package com.emarsys.api.event

import com.emarsys.api.SdkResult
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.core.collections.dequeue
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.EventType
import com.emarsys.networking.clients.event.model.Event as DeviceEvent

class EventTrackerInternal(
    private val eventClient: EventClientApi,
    private val eventTrackerContext: ApiContext<EventTrackerCall>
) : EventTrackerInstance {

    override suspend fun trackEvent(event: CustomEvent): SdkResult {
        val result = eventClient.registerEvent(DeviceEvent(EventType.CUSTOM, event.name, event.attributes))
        return SdkResult.Success(result)
        //TODO handle error
    }

    override suspend fun activate() {
        eventTrackerContext.calls.dequeue { call ->
            when(call) {
                is EventTrackerCall.TrackEvent -> eventClient.registerEvent(call.event)
            }
        }
    }
}