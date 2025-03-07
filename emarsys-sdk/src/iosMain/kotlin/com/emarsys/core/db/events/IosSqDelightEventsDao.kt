package com.emarsys.core.db.events

import com.emarsys.sqldelight.EmarsysDB
import kotlinx.serialization.json.Json

class IosSqDelightEventsDao(db: EmarsysDB, val json: Json): AbstractSqlDelightEventsDao(db, json)