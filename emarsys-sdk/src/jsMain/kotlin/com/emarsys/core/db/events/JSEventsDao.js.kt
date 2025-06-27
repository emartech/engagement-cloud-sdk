package com.emarsys.core.db.events

import com.emarsys.core.db.EmarsysIndexedDbObjectStoreApi
import com.emarsys.core.log.Logger
import com.emarsys.event.SdkEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class JSEventsDao(
    private val emarsysIndexedDbObjectStore: EmarsysIndexedDbObjectStoreApi<SdkEvent>,
    private val logger: Logger
) : EventsDaoApi {

    override suspend fun insertEvent(event: SdkEvent) {
        logger.debug(
            "insertEvent",
            buildJsonObject { put("event", event.toString()) },
            isRemoteLog = event !is SdkEvent.Internal.LogEvent
        )
        emarsysIndexedDbObjectStore.put(event.id, event)
    }

    override suspend fun upsertEvent(event: SdkEvent) {
        logger.debug(
            "upsertEvent",
            buildJsonObject { put("event", event.toString()) },
            isRemoteLog = event !is SdkEvent.Internal.LogEvent
        )
        emarsysIndexedDbObjectStore.put(event.id, event)
    }

    override suspend fun getEvents(): Flow<SdkEvent> {
        logger.debug("getEvents")
        return emarsysIndexedDbObjectStore.getAll()
    }

    override suspend fun removeEvent(event: SdkEvent) {
        if (event !is SdkEvent.Internal.LogEvent) {
            logger.debug(
                "removeEvent",
                buildJsonObject { put("event", event.toString()) },
                isRemoteLog = event !is SdkEvent.Internal.LogEvent
            )
        }
        emarsysIndexedDbObjectStore.delete(event.id)
    }

    override suspend fun removeAll() {
        logger.debug("removeAll")
        emarsysIndexedDbObjectStore.removeAll()
    }


}