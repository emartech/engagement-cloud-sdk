package com.emarsys.core.log

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.event.SdkEvent
import kotlinx.serialization.json.JsonObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class RemoteLogger : RemoteLoggerApi, KoinComponent {
    private val sdkEventDistributor: SdkEventDistributorApi by inject()
    private val sdkContext: SdkContextApi by inject()

    override suspend fun logToRemote(level: LogLevel, log: JsonObject) {
        if (sdkContext.remoteLogLevel.priority >= level.priority) {
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.Log(
                    level = level,
                    attributes = log
                )
            )
        }
    }
}