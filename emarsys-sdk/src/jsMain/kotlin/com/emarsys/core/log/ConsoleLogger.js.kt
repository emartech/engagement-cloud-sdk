package com.emarsys.core.log

import kotlinx.serialization.json.JsonObject

internal actual class ConsoleLogger : ConsoleLoggerApi {
    actual override fun logToConsole(
        loggerName: String,
        level: LogLevel,
        message: String?,
        throwable: Throwable?,
        data: JsonObject
    ) {
        val logString = createLogString(loggerName, level, message, throwable, data)
        val color = getLogColor(level)
        println(colorizeLog(logString, color))
    }

    private fun createLogString(
        loggerName: String,
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

    private fun colorizeLog(log: String, color: String): String {
        return log.split("\n").joinToString("\n") { "$color$it${"\u001B[0m"}" }
    }

    private fun getLogColor(level: LogLevel): String {
        return when (level) {
            LogLevel.Info -> "\u001B[0m"
            LogLevel.Trace -> "\u001B[35m"
            LogLevel.Debug -> "\u001B[34m"
            LogLevel.Error -> "\u001b[31m"
            LogLevel.Metric -> "\u001B[30m"
        }
    }
}