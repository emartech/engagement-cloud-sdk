package com.sap.ec.core.log

import com.sap.ec.InternalSdkApi
import kotlinx.serialization.json.JsonObject

//needs to be exposed for ServiceWorker
@InternalSdkApi
expect class ConsoleLogger() : ConsoleLoggerApi {

    override fun logToConsole(
        loggerName: String,
        level: LogLevel,
        message: String?,
        throwable: Throwable?,
        data: JsonObject
    )
}