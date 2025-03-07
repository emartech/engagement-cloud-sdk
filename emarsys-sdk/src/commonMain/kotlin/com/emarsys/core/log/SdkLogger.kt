package com.emarsys.core.log

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.coroutines.coroutineContext

class SdkLogger(
    private val consoleLogger: ConsoleLogger,
    private val remoteLogger: RemoteLogger? = null
) : Logger {
    private val queue = ArrayDeque<Pair<String, JsonObject>>(10)

    override suspend fun info(logEntry: LogEntry) {
        log(logEntry.topic, LogLevel.Info, data = logEntry.data)
    }

    override suspend fun info(tag: String, message: String) {
        log(tag, LogLevel.Info, message)
    }

    override suspend fun info(tag: String, throwable: Throwable) {
        log(tag, LogLevel.Info, throwable = throwable, data = buildJsonObject {
            put("exception", JsonPrimitive(throwable.toString()))
            put("reason", JsonPrimitive(throwable.message))
            put("stackTrace", JsonPrimitive(throwable.stackTraceToString()))
        })
    }

    override suspend fun info(tag: String, message: String, data: JsonObject) {
        log(tag, LogLevel.Info, message, data = data)
    }

    override suspend fun debug(logEntry: LogEntry) {
        log(logEntry.topic, LogLevel.Debug, data = logEntry.data)
    }

    override suspend fun debug(tag: String, message: String) {
        log(tag, LogLevel.Debug, message)
    }

    override suspend fun debug(tag: String, data: JsonObject) {
        log(tag, LogLevel.Debug, data = data)
    }

    override suspend fun debug(tag: String) {
        log(tag, LogLevel.Debug)
    }

    override suspend fun debug(tag: String, message: String, data: JsonObject) {
        log(tag, LogLevel.Debug, message, data = data)
    }

    override suspend fun debug(tag: String, throwable: Throwable) {
        log(tag, LogLevel.Debug, throwable = throwable, data = buildJsonObject {
            put("exception", JsonPrimitive(throwable.toString()))
            put("reason", JsonPrimitive(throwable.message))
            put("stackTrace", JsonPrimitive(throwable.stackTraceToString()))
        })
    }

    override suspend fun error(logEntry: LogEntry) {
        log(logEntry.topic, LogLevel.Error, data = logEntry.data)
    }

    override suspend fun error(tag: String, message: String) {
        log(tag, LogLevel.Error, message)
    }

    override suspend fun error(tag: String, data: JsonObject) {
        log(tag, LogLevel.Error, data = data)
    }

    override suspend fun error(tag: String, throwable: Throwable) {
        log(tag, LogLevel.Error, throwable = throwable, data = buildJsonObject {
            put("exception", JsonPrimitive(throwable.toString()))
            put("reason", JsonPrimitive(throwable.message))
            put("stackTrace", JsonPrimitive(throwable.stackTraceToString()))
        })
    }

    override suspend fun error(tag: String, throwable: Throwable, data: JsonObject) {
        log(tag, LogLevel.Error, throwable = throwable, data = buildJsonObject {
            put("exception", JsonPrimitive(throwable.toString()))
            put("reason", JsonPrimitive(throwable.message))
            put("stackTrace", JsonPrimitive(throwable.stackTraceToString()))
            data.forEach {
                put(it.key, it.value)
            }
        })
    }

    override suspend fun error(tag: String, message: String, data: JsonObject) {
        log(tag, LogLevel.Error, message, data = data)
    }

    private suspend fun log(
        tag: String,
        level: LogLevel,
        message: String? = null,
        throwable: Throwable? = null,
        data: JsonObject = JsonObject(mapOf())
    ) {
        if (level == LogLevel.Debug || level == LogLevel.Info) {
            if (queue.size > 10) {
                queue.removeLast()
            }
            queue.addFirst(tag to data)
        }
        val contextMap = coroutineContext[LogContext.Key]?.contextMap
        val extendedMap = buildJsonObject {
            data.forEach {
                put(it.key, it.value)
            }
            contextMap?.let {
                it.entries.forEach {
                    put(it.key, it.value)
                }
            }
        }


        val logString = createLogString(level, tag, message, throwable, extendedMap)
        remoteLogger?.logToRemote(tag to extendedMap)
        consoleLogger.logToConsole(level, logString)
    }

    private fun createLogString(
        level: LogLevel,
        tag: String,
        message: String?,
        throwable: Throwable?,
        data: Map<String, Any>?
    ): String {
        var logString = "${level.name.uppercase()} (EmarysSDK) - ${tag.uppercase()} {"
        message?.let {
            logString = "$logString message: $message"
        }
        throwable?.let {
            logString = "$logString reason: ${it.cause}, stackTrace: ${it.stackTraceToString()}"
        }
        data?.let {
            if (it.isNotEmpty()) {
                logString = "$logString, data: $it"
            }
        }
        return "$logString }"
    }
}