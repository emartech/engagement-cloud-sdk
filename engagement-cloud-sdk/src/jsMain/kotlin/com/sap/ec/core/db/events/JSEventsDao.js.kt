package com.sap.ec.core.db.events

import com.sap.ec.core.db.ECIndexedDbObjectStoreApi
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class JSEventsDao(
    private val ecIndexedDbObjectStore: ECIndexedDbObjectStoreApi<SdkEvent>,
    private val logger: Logger
) : EventsDaoApi {

    override suspend fun insertEvent(event: SdkEvent) {
        logger.debug(
            "insertEvent",
            buildJsonObject { put("event", event.toString()) },
            isRemoteLog = event !is SdkEvent.Internal.LogEvent
        )
        ecIndexedDbObjectStore.put(event.id, event)
    }

    override suspend fun upsertEvent(event: SdkEvent) {
        logger.debug(
            "upsertEvent",
            buildJsonObject { put("event", event.toString()) },
            isRemoteLog = event !is SdkEvent.Internal.LogEvent
        )
        ecIndexedDbObjectStore.put(event.id, event)
    }

    override suspend fun getEvents(): Flow<SdkEvent> {
        logger.debug("getEvents")
        return ecIndexedDbObjectStore.getAll()
    }

    override suspend fun removeEvent(event: SdkEvent) {
        if (event !is SdkEvent.Internal.LogEvent) {
            logger.debug(
                "removeEvent",
                buildJsonObject { put("event", event.toString()) },
                isRemoteLog = event !is SdkEvent.Internal.LogEvent
            )
        }
        ecIndexedDbObjectStore.delete(event.id)
    }

    override suspend fun removeAll() {
        logger.debug("removeAll")
        ecIndexedDbObjectStore.removeAll()
    }


}