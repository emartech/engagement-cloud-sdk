package com.emarsys.core.db.events

import com.emarsys.core.db.EmarsysIndexedDbObjectStoreApi
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class JSEventsDao(
    private val emarsysIndexedDbObjectStore: EmarsysIndexedDbObjectStoreApi<SdkEvent>,
    private val logger: Logger
) : EventsDaoApi {

    override suspend fun insertEvent(event: SdkEvent) {
        logger.debug("JSEventsDao - insertEvent", buildJsonObject { put("event", JsonPrimitive(event.toString())) })
        emarsysIndexedDbObjectStore.put(event.id, event)
    }

    override suspend fun getEvents(): Flow<SdkEvent> {
        logger.debug("JSEventsDao - getEvents")
        return emarsysIndexedDbObjectStore.getAll()
    }
}