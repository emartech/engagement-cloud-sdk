package com.emarsys.core.log

import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

actual class ConsoleLogger : ConsoleLoggerApi {

    @OptIn(ExperimentalSerializationApi::class)
    val prettyJson = Json {
        prettyPrint = true
        prettyPrintIndent = " "
    }

    actual override fun logToConsole(
        loggerName: String,
        level: LogLevel,
        message: String?,
        throwable: Throwable?,
        data: JsonObject
    ) {
        val logString = createLogString(message, data)

        when (level) {
            LogLevel.Info -> Log.i(loggerName, logString, throwable)
            LogLevel.Trace -> Log.v(loggerName, logString, throwable)
            LogLevel.Debug -> Log.d(loggerName, logString, throwable)
            LogLevel.Error -> Log.e(loggerName, logString, throwable)
            LogLevel.Metric -> Log.wtf(loggerName, logString)
        }
    }

    private fun createLogString(
        message: String?,
        data: JsonObject
    ): String {
        var logString = "Emarsys SDK -"
        message?.let {
            logString = "$logString $message,"
        }
        data.let {
            if (it.isNotEmpty()) {
                logString = "$logString \n data: ${prettyJson.encodeToString(it)}"
            }
        }
        return logString
    }

}