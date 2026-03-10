package com.sap.ec.core.log

import com.sap.ec.event.SdkEvent
import kotlinx.serialization.json.JsonObject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class RemoteLogger(
    private val logEventRegistry: LogEventRegistryApi,
    private val logConfigHolder: LogConfigHolderApi
) : RemoteLoggerApi {

    override suspend fun logToRemote(level: LogLevel, log: JsonObject) {
        if (logConfigHolder.remoteLogLevel.priority <= level.priority) {
            logEventRegistry.registerLogEvent(
                SdkEvent.Internal.Sdk.Log(
                    level = level,
                    attributes = log
                )
            )
        }
    }
}