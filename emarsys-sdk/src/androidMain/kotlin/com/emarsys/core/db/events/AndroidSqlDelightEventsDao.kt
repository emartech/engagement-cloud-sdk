package com.emarsys.core.db.events

import com.emarsys.sqldelight.EmarsysDB
import kotlinx.serialization.json.Json

internal class AndroidSqlDelightEventsDao(db: EmarsysDB, json: Json) :
    AbstractSqlDelightEventsDao(db, json) {
}
