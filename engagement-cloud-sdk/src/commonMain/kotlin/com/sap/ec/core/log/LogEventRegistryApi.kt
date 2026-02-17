package com.sap.ec.core.log

import com.sap.ec.event.SdkEvent
import kotlinx.coroutines.flow.Flow

internal interface LogEventRegistryApi {
    val logEvents: Flow<SdkEvent.Internal.LogEvent>

    suspend fun registerLogEvent(logEvent: SdkEvent.Internal.LogEvent)
}