package com.emarsys.core.channel

import com.emarsys.context.SdkContextApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.OnlineSdkEvent
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.coroutineContext

class SdkEventDistributor(
    private val connectionStatus: StateFlow<Boolean>,
    private val sdkContext: SdkContextApi,
    private val eventsDao: EventsDaoApi,
    private val sdkLogger: Logger
) : SdkEventManagerApi {

    private val _sdkEventFlow = MutableSharedFlow<SdkEvent>(
        replay = 100,
        extraBufferCapacity = Channel.UNLIMITED
    )

    override val sdkEventFlow = _sdkEventFlow.asSharedFlow()

    override val onlineSdkEvents: Flow<OnlineSdkEvent> =
        _sdkEventFlow
            .filterIsInstance<OnlineSdkEvent>()
            .filter {
                it !is SdkEvent.Internal.LogEvent
            }
            .onEach {
                combine(
                    sdkContext.currentSdkState,
                    connectionStatus
                ) { sdkState, isConnected ->
                    isConnected
                }.first { it }
            }

    override val logEvents = _sdkEventFlow
        .filterIsInstance<SdkEvent.Internal.LogEvent>()
        .onEach {
            connectionStatus.first { it }
        }

    override suspend fun registerEvent(sdkEvent: SdkEvent): SdkEventWaiterApi? {
        return try {
            if (sdkEvent is OnlineSdkEvent) {
                eventsDao.insertEvent(sdkEvent)
            }
            _sdkEventFlow.emit(sdkEvent)
            SdkEventWaiter(this@SdkEventDistributor, sdkEvent)
        } catch (exception: Exception) {
            coroutineContext.ensureActive()
            sdkLogger.error(
                "SdkEventDistributor - Failed to register event",
                exception,
                buildJsonObject { put("event", sdkEvent.toString()) },
                isRemoteLog = sdkEvent !is SdkEvent.Internal.LogEvent
            )
            null
        }
    }

    override suspend fun emitEvent(sdkEvent: SdkEvent) {
        try {
            _sdkEventFlow.emit(sdkEvent)
        } catch (exception: Exception) {
            coroutineContext.ensureActive()
            sdkLogger.error(
                "SdkEventDistributor - Failed to emit event",
                exception,
                buildJsonObject { put("event", sdkEvent.toString()) })
        }
    }
}