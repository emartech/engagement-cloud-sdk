package com.emarsys.core.log

import kotlinx.serialization.json.JsonObject

interface Logger {
    suspend fun info(logEntry: LogEntry)
    suspend fun info(tag: String, message: String)
    suspend fun info(tag: String, throwable: Throwable)
    suspend fun info(tag: String, message: String, data: JsonObject)

    suspend fun debug(logEntry: LogEntry)
    suspend fun debug(tag: String)
    suspend fun debug(tag: String, message: String)
    suspend fun debug(tag: String, data: JsonObject)
    suspend fun debug(tag: String, throwable: Throwable)
    suspend fun debug(tag: String, message: String, data: JsonObject)

    suspend fun error(logEntry: LogEntry)
    suspend fun error(tag: String, message: String)
    suspend fun error(tag: String, data: JsonObject)
    suspend fun error(tag: String, throwable: Throwable)
    suspend fun error(tag: String, throwable: Throwable, data: JsonObject)
    suspend fun error(tag: String, message: String, data: JsonObject)
}