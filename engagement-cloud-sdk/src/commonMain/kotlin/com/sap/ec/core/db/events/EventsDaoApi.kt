package com.sap.ec.core.db.events

import com.sap.ec.event.SdkEvent
import kotlinx.coroutines.flow.Flow

interface EventsDaoApi {
    suspend fun insertEvent(event: SdkEvent)
    suspend fun upsertEvent(event: SdkEvent)
    suspend fun getEvents(): Flow<SdkEvent>
    suspend fun removeEvent(event: SdkEvent)
    suspend fun removeAll()
}