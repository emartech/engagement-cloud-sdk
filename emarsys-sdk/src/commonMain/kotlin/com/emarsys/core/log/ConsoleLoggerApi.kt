package com.emarsys.core.log

import kotlinx.serialization.json.JsonObject

interface ConsoleLoggerApi {

    fun logToConsole(
        loggerName: String,
        level: LogLevel,
        message: String?,
        throwable: Throwable?,
        data: JsonObject
    )
}