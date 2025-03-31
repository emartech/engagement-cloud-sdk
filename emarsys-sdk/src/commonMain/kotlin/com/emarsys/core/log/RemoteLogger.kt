package com.emarsys.core.log

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonObject

class RemoteLogger(private val sdkFlow: MutableStateFlow<SdkEvent>) {
    suspend fun logToRemote(level: LogLevel, log: Pair<String, JsonObject>) {
        sdkFlow.emit(
            SdkEvent.Internal.Sdk.Log(
                level = level,
                name = log.first,
                attributes = log.second
            )
        )
    }
}