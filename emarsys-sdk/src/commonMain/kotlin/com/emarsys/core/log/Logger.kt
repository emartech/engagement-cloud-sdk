package com.emarsys.core.log

import kotlinx.serialization.json.JsonObject

interface Logger {
    suspend fun info(logEntry: LogEntry)
    suspend fun info(message: String)
    suspend fun info(message: String, throwable: Throwable)
    suspend fun info(message: String, data: JsonObject)

    suspend fun debug(logEntry: LogEntry)
    suspend fun debug(message: String)
    suspend fun debug(message: String, data: JsonObject)
    suspend fun debug(message: String, throwable: Throwable)

    suspend fun error(logEntry: LogEntry)
    suspend fun error(message: String)
    suspend fun error(message: String, data: JsonObject)
    suspend fun error(message: String, throwable: Throwable)
    suspend fun error(message: String, throwable: Throwable, data: JsonObject)
}