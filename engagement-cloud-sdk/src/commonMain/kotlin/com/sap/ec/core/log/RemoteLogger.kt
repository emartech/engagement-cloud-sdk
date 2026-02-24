package com.sap.ec.core.log

import com.sap.ec.context.SdkContextApi
import com.sap.ec.event.SdkEvent
import kotlinx.serialization.json.JsonObject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class RemoteLogger(
    val logEventRegistry: LogEventRegistryApi,
    private val sdkContext: SdkContextApi
) : RemoteLoggerApi {

    override suspend fun logToRemote(level: LogLevel, log: JsonObject) {
        if (sdkContext.remoteLogLevel.priority <= level.priority) {
            logEventRegistry.registerLogEvent(
                SdkEvent.Internal.Sdk.Log(
                    level = level,
                    attributes = log
                )
            )
        }
    }
}