package com.emarsys.core.db.events

import com.emarsys.core.db.EventsDaoApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.sqldelight.EmarsysDB

class AndroidSqlDelightEventsDao(private val db: EmarsysDB): EventsDaoApi {

    override fun insertEvent(event: SdkEvent) {
    }

    override fun getEvents(): List<SdkEvent> {
        return emptyList()
    }
}