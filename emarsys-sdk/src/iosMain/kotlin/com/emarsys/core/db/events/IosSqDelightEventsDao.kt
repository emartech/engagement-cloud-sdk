package com.emarsys.core.db.events

import com.emarsys.sqldelight.EmarsysDB
import kotlinx.serialization.json.Json

internal class IosSqDelightEventsDao(db: EmarsysDB, val json: Json): AbstractSqlDelightEventsDao(db, json)