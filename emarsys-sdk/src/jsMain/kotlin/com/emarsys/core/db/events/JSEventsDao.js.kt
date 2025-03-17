package com.emarsys.core.db.events

import com.emarsys.core.db.EmarsysIndexedDbObjectStoreApi
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class JSEventsDao(
    private val emarsysIndexedDbObjectStore: EmarsysIndexedDbObjectStoreApi<SdkEvent>,
    private val logger: Logger
) : EventsDaoApi {

    override suspend fun insertEvent(event: SdkEvent) {
        logger.debug(
            "JSEventsDao - insertEvent",
            buildJsonObject { put("event", event.toString()) })
        emarsysIndexedDbObjectStore.put(event.id, event)
    }

    override suspend fun getEvents(): Flow<SdkEvent> {
        logger.debug("JSEventsDao - getEvents")
        return emarsysIndexedDbObjectStore.getAll()
    }

    override suspend fun removeEvent(event: SdkEvent) {
        logger.debug(
            "JSEventsDao - removeEvent",
            buildJsonObject { put("event", event.toString()) }
        )
        emarsysIndexedDbObjectStore.delete(event.id)
    }


}