package com.emarsys.core.log

import com.emarsys.event.SdkEvent
import kotlinx.coroutines.flow.Flow

internal interface LogEventRegistryApi {
    val logEvents: Flow<SdkEvent.Internal.LogEvent>

    suspend fun registerLogEvent(logEvent: SdkEvent.Internal.LogEvent)
}