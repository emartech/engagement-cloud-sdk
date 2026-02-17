package com.sap.ec.core.db.events

import com.sap.ec.sqldelight.SapEngagementCloudDB
import kotlinx.serialization.json.Json

internal class IosSqDelightEventsDao(db: SapEngagementCloudDB, val json: Json): AbstractSqlDelightEventsDao(db, json)