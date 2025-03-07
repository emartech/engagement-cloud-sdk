package com.emarsys.core.db.events

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class JSEventsDao : EventsDaoApi {

    override suspend fun insertEvent(event: SdkEvent) {
    }

    override suspend fun getEvents(): Flow<SdkEvent> {
        return flowOf()
    }
}