package com.emarsys.core.db.events

import com.emarsys.event.SdkEvent
import kotlinx.coroutines.flow.Flow

interface EventsDaoApi {
    suspend fun insertEvent(event: SdkEvent)
    suspend fun upsertEvent(event: SdkEvent)
    suspend fun getEvents(): Flow<SdkEvent>
    suspend fun removeEvent(event: SdkEvent)
    suspend fun removeAll()
}