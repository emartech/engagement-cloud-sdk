package com.emarsys.core.db.events

import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.sqldelight.EmarsysDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.serialization.json.Json

internal abstract class AbstractSqlDelightEventsDao(db: EmarsysDB, private val json: Json) : EventsDaoApi {
    private val queries = db.eventsQueries

    override suspend fun insertEvent(event: SdkEvent) {
        queries.insertEvent(
            id = event.id,
            type = event.type,
            name = event.name,
            timestamp = event.timestamp.toEpochMilliseconds(),
            attributes = event.attributes.toString(),
            json = json.encodeToString(event)
        )
    }

    override suspend fun getEvents(): Flow<SdkEvent> {
        return queries.selectAll()
            .executeAsList()
            .map {
                json.decodeFromString<SdkEvent>(it)
            }
            .asFlow()
    }

    override suspend fun removeEvent(event: SdkEvent) {
        queries.deleteById(event.id)
    }

    override suspend fun removeAll() {
        queries.deleteAll()
    }
}