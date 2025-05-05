package com.emarsys.core.log

import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.coroutineContext

internal class SdkLogger(
    private val loggerName: String,
    private val consoleLogger: ConsoleLoggerApi,
    private val remoteLogger: RemoteLoggerApi? = null,
    private val sdkContext: SdkContextApi? = null
) : Logger {

    companion object {
        val mutex = Mutex()
        val breadcrumbsQueue = ArrayDeque<Pair<String, JsonObject>>(10)
    }

    override suspend fun info(logEntry: LogEntry, isRemoteLog: Boolean) {
        log(LogLevel.Info, data = logEntry.data, isRemoteLog = isRemoteLog)
    }

    override suspend fun info(message: String, isRemoteLog: Boolean) {
        log(LogLevel.Info, message, isRemoteLog = isRemoteLog)
    }

    override suspend fun info(message: String, throwable: Throwable, isRemoteLog: Boolean) {
        log(LogLevel.Info, throwable = throwable, message = message, isRemoteLog = isRemoteLog)
    }

    override suspend fun info(message: String, data: JsonObject, isRemoteLog: Boolean) {
        log(LogLevel.Info, message, data = data, isRemoteLog = isRemoteLog)
    }

    override suspend fun debug(logEntry: LogEntry, isRemoteLog: Boolean) {
        log(
            LogLevel.Debug,
            message = logEntry.topic,
            data = logEntry.data,
            isRemoteLog = isRemoteLog
        )
    }

    override suspend fun debug(message: String, isRemoteLog: Boolean) {
        log(LogLevel.Debug, message, isRemoteLog = isRemoteLog)
    }

    override suspend fun debug(message: String, data: JsonObject, isRemoteLog: Boolean) {
        log(LogLevel.Debug, message, data = data, isRemoteLog = isRemoteLog)
    }

    override suspend fun debug(message: String, throwable: Throwable, isRemoteLog: Boolean) {
        log(LogLevel.Debug, message = message, throwable = throwable, isRemoteLog = isRemoteLog)
    }

    override suspend fun error(logEntry: LogEntry, isRemoteLog: Boolean) {
        log(
            LogLevel.Error,
            message = logEntry.topic,
            data = logEntry.data,
            isRemoteLog = isRemoteLog
        )
    }

    override suspend fun error(message: String, isRemoteLog: Boolean) {
        log(LogLevel.Error, message, isRemoteLog = isRemoteLog)
    }

    override suspend fun error(message: String, data: JsonObject, isRemoteLog: Boolean) {
        log(LogLevel.Error, message = message, data = data, isRemoteLog = isRemoteLog)
    }

    override suspend fun error(message: String, throwable: Throwable, isRemoteLog: Boolean) {
        log(LogLevel.Error, throwable = throwable, message = message, isRemoteLog = isRemoteLog)
    }

    override suspend fun error(
        message: String,
        throwable: Throwable,
        data: JsonObject,
        isRemoteLog: Boolean
    ) {
        log(
            LogLevel.Error,
            message = message,
            throwable = throwable,
            data = data,
            isRemoteLog = isRemoteLog
        )
    }

    override suspend fun metric(
        message: String,
        data: JsonObject
    ) {
        log(LogLevel.Metric, message = message, data = data, isRemoteLog = true)
    }

    private suspend fun log(
        level: LogLevel,
        message: String? = null,
        throwable: Throwable? = null,
        data: JsonObject = JsonObject(mapOf()),
        isRemoteLog: Boolean = true
    ) {
        val contextMap = coroutineContext[LogContext.Key]?.contextMap
        val extendedData = mergeContext(data, contextMap)

        if (remoteLogger != null && isRemoteLog) {
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

        consoleLogger.logToConsole(loggerName, level, message, throwable, extendedData)
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


    private fun createLogObject(
        level: LogLevel,
        message: String?,
        throwable: Throwable?,
        data: JsonObject,
        includeBreadcrumbs: Boolean = false
    ) = buildJsonObject {
        put("loggerName", loggerName)
        put("level", level.name.uppercase())
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