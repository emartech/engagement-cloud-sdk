package com.sap.ec.core.log

import com.sap.ec.event.SdkEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class LogEventRegistry : LogEventRegistryApi {
    private val _logEvents: MutableSharedFlow<SdkEvent.Internal.LogEvent> = MutableSharedFlow(
        replay = 100,
        extraBufferCapacity = Channel.UNLIMITED
    )
    override val logEvents: Flow<SdkEvent.Internal.LogEvent> = _logEvents.asSharedFlow()

    override suspend fun registerLogEvent(logEvent: SdkEvent.Internal.LogEvent) {
        _logEvents.emit(logEvent)
    }
}