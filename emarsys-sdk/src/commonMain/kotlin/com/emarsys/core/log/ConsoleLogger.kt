package com.emarsys.core.log

import kotlinx.serialization.json.JsonObject

expect class ConsoleLogger() {

    fun logToConsole(
        loggerName: String,
        level: LogLevel,
        message: String?,
        throwable: Throwable?,
        data: JsonObject
    )
}