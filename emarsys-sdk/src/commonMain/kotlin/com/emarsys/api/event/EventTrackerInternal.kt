package com.emarsys.api.event

import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.EventType
import com.emarsys.networking.clients.event.model.Event as DeviceEvent

class EventTrackerInternal(private val eventClient: EventClientApi) : EventTrackerInstance {

    override suspend fun trackEvent(event: CustomEvent) {
        eventClient.registerEvent(DeviceEvent(EventType.CUSTOM, event.name, event.attributes))
    }

    override suspend fun activate() {
    }

}