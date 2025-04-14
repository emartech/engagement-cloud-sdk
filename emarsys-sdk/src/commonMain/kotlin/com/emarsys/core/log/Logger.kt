package com.emarsys.core.log

import kotlinx.serialization.json.JsonObject

interface Logger {
    suspend fun info(logEntry: LogEntry, isRemoteLog: Boolean = true)
    suspend fun info(message: String, isRemoteLog: Boolean = true)
    suspend fun info(message: String, throwable: Throwable, isRemoteLog: Boolean = true)
    suspend fun info(message: String, data: JsonObject, isRemoteLog: Boolean = true)

    suspend fun debug(logEntry: LogEntry, isRemoteLog: Boolean = true)
    suspend fun debug(message: String, isRemoteLog: Boolean = true)
    suspend fun debug(message: String, data: JsonObject, isRemoteLog: Boolean = true)
    suspend fun debug(message: String, throwable: Throwable, isRemoteLog: Boolean = true)

    suspend fun error(logEntry: LogEntry, isRemoteLog: Boolean = true)
    suspend fun error(message: String, isRemoteLog: Boolean = true)
    suspend fun error(message: String, data: JsonObject, isRemoteLog: Boolean = true)
    suspend fun error(message: String, throwable: Throwable, isRemoteLog: Boolean = true)
    suspend fun error(
        message: String,
        throwable: Throwable,
        data: JsonObject,
        isRemoteLog: Boolean = true
    )

    suspend fun metric(message: String, data: JsonObject)
}