package com.emarsys.core.log

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.coroutineContext

class SdkLogger(
    private val loggerName: String,
    private val consoleLogger: ConsoleLogger,
    private val remoteLogger: RemoteLogger? = null
) : Logger {
    private val queue = ArrayDeque<Pair<String, JsonObject>>(10)

    override suspend fun info(logEntry: LogEntry) {
        log(LogLevel.Info, data = logEntry.data)
    }

    override suspend fun info(message: String) {
        log(LogLevel.Info, message)
    }

    override suspend fun info(message: String, throwable: Throwable) {
        log(LogLevel.Info, throwable = throwable, message = message, data = buildJsonObject {
            put("exception", throwable.toString())
            put("reason", throwable.message)
            put("stackTrace", throwable.stackTraceToString())
        })
    }

    override suspend fun info(message: String, data: JsonObject) {
        log(LogLevel.Info, message, data = data)
    }

    override suspend fun debug(logEntry: LogEntry) {
        log(LogLevel.Debug, message = logEntry.topic, data = logEntry.data)
    }

    override suspend fun debug(message: String) {
        log(LogLevel.Debug, message)
    }

    override suspend fun debug(message: String, data: JsonObject) {
        log(LogLevel.Debug, message, data = data)
    }

    override suspend fun debug(message: String, throwable: Throwable) {
        log(LogLevel.Debug, message = message, throwable = throwable, data = buildJsonObject {
            put("exception", throwable.toString())
            put("reason", throwable.message)
            put("stackTrace", throwable.stackTraceToString())
        })
    }

    override suspend fun error(logEntry: LogEntry) {
        log(LogLevel.Error, message = logEntry.topic, data = logEntry.data)
    }

    override suspend fun error(message: String) {
        log(LogLevel.Error, message)
    }

    override suspend fun error(message: String, data: JsonObject) {
        log(LogLevel.Error, message = message, data = data)
    }

    override suspend fun error(message: String, throwable: Throwable) {
        log(LogLevel.Error, throwable = throwable, message = message, data = buildJsonObject {
            put("exception", throwable.toString())
            put("reason", throwable.message)
            put("stackTrace", throwable.stackTraceToString())
        })
    }

    override suspend fun error(
        message: String,
        throwable: Throwable,
        data: JsonObject
    ) {
        log(LogLevel.Error, message = message, throwable = throwable, data = buildJsonObject {
            put("exception", throwable.toString())
            put("reason", throwable.message)
            put("stackTrace", throwable.stackTraceToString())
            data.forEach {
                put(it.key, it.value)
            }
        })
    }

    private suspend fun log(
        level: LogLevel,
        message: String? = null,
        throwable: Throwable? = null,
        data: JsonObject = JsonObject(mapOf())
    ) {
        if (level == LogLevel.Debug || level == LogLevel.Info) {
            if (queue.size > 10) {
                queue.removeLast()
            }
            queue.addFirst(loggerName to data)
        }
        val contextMap = coroutineContext[LogContext.Key]?.contextMap
        val extendedMap = buildJsonObject {
            data.forEach {
                put(it.key, it.value)
            }
            contextMap?.let {
                it.entries.forEach { entry ->
                    put(entry.key, entry.value)
                }
            }
//            put("breadcrumbs", buildJsonObject {
//                queue.forEachIndexed { index, entry ->
//                    put("entry_$index", entry.second)
//                }
//            })
        }
        val remoteMap = buildJsonObject {
            extendedMap.forEach { put(it.key, it.value) }
            put("loggerName", loggerName)
        }

        val logString = createLogString(level, loggerName, message, throwable, extendedMap)
        remoteLogger?.logToRemote(level, loggerName to remoteMap)
        consoleLogger.logToConsole(level, logString)
    }

    private fun createLogString(
        level: LogLevel,
        loggerName: String,
        message: String?,
        throwable: Throwable?,
        data: JsonObject
    ): String {

        var logString = "${level.name.uppercase()} (EmarysSDK) - $loggerName: {"
        message?.let {
            logString = "$logString message: $message"
        }
        throwable?.let {
            logString = "$logString reason: ${it.cause}, stackTrace: ${it.stackTraceToString()}"
        }
        data.let {
            if (it.isNotEmpty()) {
                logString = "$logString, data: $it"
            }
        }
        return "$logString }"
    }
}