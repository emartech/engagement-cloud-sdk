package com.emarsys.core.db.events

import com.emarsys.core.db.EventsDaoApi
import com.emarsys.networking.clients.event.model.SdkEvent

class JSEventsDao: EventsDaoApi {

    override fun insertEvent(event: SdkEvent) {
    }

    override fun getEvents(): List<SdkEvent> {
        return emptyList()
    }
}