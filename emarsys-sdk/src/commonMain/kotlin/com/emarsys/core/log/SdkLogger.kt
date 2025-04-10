package com.emarsys.core.log

import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.coroutineContext

class SdkLogger(
    private val loggerName: String,
    private val consoleLogger: ConsoleLogger,
    private val remoteLogger: RemoteLoggerApi? = null,
    private val sdkContext: SdkContextApi? = null
) : Logger {

    companion object {
        val mutex = Mutex()
        val breadcrumbsQueue = ArrayDeque<Pair<String, JsonObject>>(10)
    }

    override suspend fun info(logEntry: LogEntry) {
        log(LogLevel.Info, data = logEntry.data)
    }

    override suspend fun info(message: String) {
        log(LogLevel.Info, message)
    }

    override suspend fun info(message: String, throwable: Throwable) {
        log(LogLevel.Info, throwable = throwable, message = message)
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
        log(LogLevel.Debug, message = message, throwable = throwable)
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
        log(LogLevel.Error, throwable = throwable, message = message)
    }

    override suspend fun error(
        message: String,
        throwable: Throwable,
        data: JsonObject
    ) {
        log(LogLevel.Error, message = message, throwable = throwable, data = data)
    }

    override suspend fun metric(
        message: String,
        data: JsonObject
    ) {
        log(LogLevel.Metric, message = message, data = data)
    }

    private suspend fun log(
        level: LogLevel,
        message: String? = null,
        throwable: Throwable? = null,
        data: JsonObject = JsonObject(mapOf())
    ) {
        val contextMap = coroutineContext[LogContext.Key]?.contextMap
        val extendedData = mergeContext(data, contextMap)

        if (remoteLogger != null) {
            if (level == LogLevel.Debug || level == LogLevel.Info) {
                mutex.withLock {
                    if (breadcrumbsQueue.size >= (sdkContext?.logBreadcrumbsQueueSize ?: 10)) {
                        breadcrumbsQueue.removeLast()
                    }
                    val breadcrumbLog =
                        createLogObject(level, message, throwable, data)
                    breadcrumbsQueue.addFirst(loggerName to breadcrumbLog)
                }
            }
            val remoteLog =
                createLogObject(level, message, throwable, extendedData, includeBreadcrumbs = true)
            remoteLogger.logToRemote(level, remoteLog)
        }

        val logString = createLogString(level, message, throwable, extendedData)
        consoleLogger.logToConsole(level, logString)
    }

    private fun mergeContext(
        data: JsonObject,
        contextMap: JsonObject?
    ) = buildJsonObject {
        data.forEach {
            put(it.key, it.value)
        }
        contextMap?.let {
            it.entries.forEach { entry ->
                put(entry.key, entry.value)
            }
        }
    }

    private fun createLogString(
        level: LogLevel,
        message: String?,
        throwable: Throwable?,
        data: JsonObject
    ): String {
        var logString = "${level.name.uppercase()} (EmarysSDK) - $loggerName: {"
        message?.let {
            logString = "$logString message: $message,"
        }
        throwable?.let {
            logString = "$logString reason: ${it.cause}, stackTrace: ${it.stackTraceToString()},"
        }
        data.let {
            if (it.isNotEmpty()) {
                logString = "$logString data: $it"
            }
        }
        return "$logString }"
    }

    private fun createLogObject(
        level: LogLevel,
        message: String?,
        throwable: Throwable?,
        data: JsonObject,
        includeBreadcrumbs: Boolean = false
    ) = buildJsonObject {
        put("loggerName", loggerName)
        put("level", level.name)
        put("message", message ?: "")
        throwable?.let {
            put("exception", throwable.toString())
            put("reason", throwable.message)
            put("stackTrace", throwable.stackTraceToString())
        }
        data.forEach {
            put(it.key, it.value)
        }
        if (includeBreadcrumbs && level == LogLevel.Error && breadcrumbsQueue.isNotEmpty()) {
            put("breadcrumbs", buildJsonObject {
                breadcrumbsQueue.forEachIndexed { index, entry ->
                    put("entry_$index", entry.second)
                }
            })
        }

    }
}