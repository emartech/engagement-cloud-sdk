package com.emarsys.core.log

import kotlinx.serialization.json.JsonObject

 internal interface RemoteLoggerApi {
    suspend fun logToRemote(level: LogLevel, log: JsonObject)
}