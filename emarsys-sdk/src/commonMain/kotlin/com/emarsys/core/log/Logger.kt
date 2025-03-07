package com.emarsys.core.log

interface Logger {
    fun log(entry: LogEntry, level: LogLevel)

    suspend fun info(logEntry: LogEntry)
    suspend fun info(tag: String, message: String)
    suspend fun info(tag: String, throwable: Throwable)
    suspend fun info(tag: String, message: String, data: Map<String, Any>)

    suspend fun debug(logEntry: LogEntry)
    suspend fun debug(tag: String)
    suspend fun debug(tag: String, message: String)
    suspend fun debug(tag: String, data: Map<String,Any>)
    suspend fun debug(tag: String, throwable: Throwable)
    suspend fun debug(tag: String, message: String, data: Map<String, Any>)

    suspend fun error(logEntry: LogEntry)
    suspend fun error(tag: String, message: String)
    suspend fun error(tag: String, throwable: Throwable)
    suspend fun error(tag: String, throwable: Throwable, data: Map<String, Any>)
    suspend fun error(tag: String, message: String, data: Map<String, Any>)
}