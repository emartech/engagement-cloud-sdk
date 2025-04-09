package com.emarsys.core.log

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.json.JsonObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class RemoteLogger : RemoteLoggerApi, KoinComponent {
    private val sdkEventDistributor: SdkEventDistributorApi by inject()

    override suspend fun logToRemote(level: LogLevel, log: JsonObject) {
        sdkEventDistributor.registerAndStoreLogEvent(
            SdkEvent.Internal.Sdk.Log(
                level = level,
                attributes = log
            )
        )
    }
}