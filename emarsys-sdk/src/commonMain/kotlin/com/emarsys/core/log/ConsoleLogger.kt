package com.emarsys.core.log

import kotlinx.serialization.json.JsonObject

internal expect class ConsoleLogger() : ConsoleLoggerApi {

    override fun logToConsole(
        loggerName: String,
        level: LogLevel,
        message: String?,
        throwable: Throwable?,
        data: JsonObject
    )
}