package com.emarsys.core.db.events

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.Flow

interface EventsDaoApi {
    suspend fun insertEvent(event: SdkEvent)
    suspend fun getEvents(): Flow<SdkEvent>
    suspend fun removeEvent(event: SdkEvent)
    suspend fun removeAll()
}