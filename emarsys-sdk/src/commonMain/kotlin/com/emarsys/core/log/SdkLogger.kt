package com.emarsys.core.log

import kotlin.coroutines.coroutineContext

class SdkLogger(private val consoleLogger: ConsoleLogger): Logger {

    override fun log(entry: LogEntry, level: LogLevel) {
        println("LogEntry: $entry logLevel: $level")
    }

    override suspend fun info(logEntry: LogEntry) {
        log(logEntry.topic, LogLevel.Info, data = logEntry.data)
    }

    override suspend fun info(tag: String, message: String) {
        log(tag, LogLevel.Info, message)
    }

    override suspend fun info(tag: String, throwable: Throwable) {
        log(tag, LogLevel.Info, throwable = throwable)
    }

    override suspend fun info(tag: String, message: String, data: Map<String, Any>) {
        log(tag, LogLevel.Info, message, data = data)
    }

    override suspend fun debug(logEntry: LogEntry) {
        log(logEntry.topic, LogLevel.Debug, data = logEntry.data)
    }

    override suspend fun debug(tag: String, message: String) {
        log(tag, LogLevel.Debug, message)
    }

    override suspend fun debug(tag: String, message: String, data: Map<String, Any>) {
        log(tag, LogLevel.Debug, message, data = data)
    }

    override suspend fun debug(tag: String, throwable: Throwable) {
        log(tag, LogLevel.Debug, throwable = throwable)
    }

    override suspend fun error(logEntry: LogEntry) {
        log(logEntry.topic, LogLevel.Error, data = logEntry.data)
    }

    override suspend fun error(tag: String, message: String) {
        log(tag, LogLevel.Error, message)
    }

    override suspend fun error(tag: String, throwable: Throwable) {
        log(tag, LogLevel.Error, throwable = throwable)
    }

    override suspend fun error(tag: String, throwable: Throwable, data: Map<String, Any>) {
        log(tag, LogLevel.Error, throwable = throwable, data = data)
    }

    override suspend fun error(tag: String, message: String, data: Map<String, Any>) {
        log(tag, LogLevel.Error, message, data = data)
    }

    private suspend fun log(
        tag: String,
        level: LogLevel,
        message: String? = null,
        throwable: Throwable? = null,
        data: Map<String, Any>? = null
    )  {
        val contextMap = coroutineContext[LogContext.Key]?.contextMap
        val extendedMap = mutableMapOf<String, Any>()
        data?.let {
            extendedMap.putAll(it)
        }
        contextMap?.let {
            extendedMap.putAll(it)
        }
        val logString = createLogString(level, tag, message, throwable, extendedMap)
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