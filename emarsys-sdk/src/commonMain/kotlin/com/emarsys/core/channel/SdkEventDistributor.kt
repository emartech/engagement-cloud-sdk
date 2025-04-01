package com.emarsys.core.channel

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.SdkEventEmitterApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.measureTime

class SdkEventDistributor(
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val connectionStatus: StateFlow<Boolean>,
    private val sdkContext: SdkContextApi,
    private val eventsDao: EventsDaoApi,
    private val sdkLogger: Logger
) : SdkEventDistributorApi, SdkEventEmitterApi {

    val onlineEvents =
        sdkEventFlow.onEach {
            combine(
                sdkContext.currentSdkState,
                connectionStatus
            ) { sdkState, isConnected ->
                sdkState == SdkState.active && isConnected
            }.first { it }
        }


    override suspend fun registerEvent(sdkEvent: SdkEvent) {
        try {
            // todo remove
            measureTime {
                eventsDao.insertEvent(sdkEvent)
            }.let {
                sdkLogger.debug(
                    "SdkEventDistributor - Event inserted into DB in ${it.inWholeMilliseconds} ms"
                )
            }
            sdkEventFlow.emit(sdkEvent)
        } catch (exception: Exception) {
            sdkLogger.error(
                "SdkEventDistributor - Failed to register event",
                exception,
                buildJsonObject { put("event", sdkEvent.toString()) })
        }
    }

    override suspend fun emitEvent(sdkEvent: SdkEvent) {
        try {
            sdkEventFlow.emit(sdkEvent)
        } catch (exception: Exception) {
            sdkLogger.error(
                "SdkEventDistributor - Failed to emit event",
                exception,
                buildJsonObject { put("event", sdkEvent.toString()) })
        }
    }
}