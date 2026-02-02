package com.emarsys.core.log

import com.emarsys.context.SdkContextApi
import com.emarsys.event.SdkEvent
import kotlinx.serialization.json.JsonObject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class RemoteLogger(
    val logEventRegistry: LogEventRegistryApi,
    private val sdkContext: SdkContextApi
) : RemoteLoggerApi {

    override suspend fun logToRemote(level: LogLevel, log: JsonObject) {
        if (sdkContext.remoteLogLevel.priority >= level.priority) {
            logEventRegistry.registerLogEvent(
                SdkEvent.Internal.Sdk.Log(
                    level = level,
                    attributes = log
                )
            )
        }
    }
}