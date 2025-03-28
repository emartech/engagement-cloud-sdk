package com.emarsys.core.channel

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach

class SdkEventDistributor(
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val connectionStatus: StateFlow<Boolean>,
    private val sdkContext: SdkContextApi,
    private val eventsDao: EventsDaoApi,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger
) {
    private val _onlineEvents =
        MutableSharedFlow<SdkEvent>(replay = 100, extraBufferCapacity = Channel.UNLIMITED)

    val onlineEvents =
        sdkEventFlow.onEach {
            sdkContext.currentSdkState.combine(connectionStatus) { sdkState, isConnected ->
                sdkState == SdkState.active && isConnected
            }.first { it }
        }

    suspend fun registerEvent(sdkEvent: SdkEvent) {
        sdkEventFlow.emit(sdkEvent)
    }

}