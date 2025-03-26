package com.emarsys.core.channel

import com.emarsys.core.Registerable
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SdkEventDistributor(
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val connectionStatus: StateFlow<Boolean>,
    private val eventsDao: EventsDaoApi,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger
) : Registerable {
    private val _onlineEvents =
        MutableSharedFlow<SdkEvent>(replay = 100, extraBufferCapacity = Channel.UNLIMITED)
    val onlineEvents = _onlineEvents.asSharedFlow()

    private val isDbReemissionInProgress = MutableStateFlow(false)

    override suspend fun register() {
        watchConnectionStatus()
        startDistribution()
    }

    private fun watchConnectionStatus() {
        CoroutineScope(sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
            connectionStatus.onEach { isConnected ->
                if (isConnected) {
                    try {
                        isDbReemissionInProgress.emit(true)
                        eventsDao.getEvents().collect {
                            _onlineEvents.emit(it)
                            eventsDao.removeEvent(it)
                        }
                    } catch (exception: Exception) {
                        sdkLogger.error("SdkEventDistributor - watchConnectionStatus", exception)
                    } finally {
                        isDbReemissionInProgress.emit(false)
                    }
                }
            }.collect()
        }
    }

    private fun startDistribution() {
        CoroutineScope(sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
            sdkEventFlow
                .onEach {
                    isDbReemissionInProgress.first { !it }
                }.onEach { sdkEvent ->
                    if (!connectionStatus.value) {
                        persistSdkEvent(sdkEvent)
                    } else {
                        _onlineEvents.emit(sdkEvent)
                    }
                }.collect()
        }
    }

    private suspend fun persistSdkEvent(sdkEvent: SdkEvent) {
        try {
            eventsDao.insertEvent(sdkEvent)
        } catch (exception: Exception) {
            sdkLogger.error("SdkEventDistributor - persistSdkEvent", exception,
                buildJsonObject {
                    put("sdkEvent", sdkEvent.toString())
                })
        }
    }

}