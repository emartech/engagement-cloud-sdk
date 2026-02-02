package com.emarsys.core.channel

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.Registerable
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.LogEventRegistryApi
import com.emarsys.core.log.Logger
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class SdkEventDistributor(
    private val connectionStatus: StateFlow<Boolean>,
    private val sdkContext: SdkContextApi,
    private val eventsDao: EventsDaoApi,
    private val logEventRegistry: LogEventRegistryApi,
    private val applicationScope: CoroutineScope,
    private val sdkLogger: Logger
) : SdkEventManagerApi, Registerable {

    private val _sdkEventFlow = MutableSharedFlow<SdkEvent>(
        replay = 100,
        extraBufferCapacity = Channel.UNLIMITED
    )

    override val sdkEventFlow = _sdkEventFlow.asSharedFlow()

    private val _onlineSdkEvents: Flow<OnlineSdkEvent> =
        _sdkEventFlow
            .filterIsInstance<OnlineSdkEvent>()
            .filter { it !is SdkEvent.Internal.LogEvent }
            .onEach { connectionStatus.first { it } }

    private val setupFlowOnlineEvents =
        _onlineSdkEvents
            .filterIsInstance<SdkEvent.Internal.SetupFlowEvent>()
            .onEach { sdkContext.currentSdkState.first { it == SdkState.Active || it == SdkState.OnHold } }

    private val nonSetupFlowOnlineEvents =
        _onlineSdkEvents
            .filter { it !is SdkEvent.Internal.SetupFlowEvent }
            .onEach { sdkContext.currentSdkState.first { it == SdkState.Active } }

    override val onlineSdkEvents: Flow<OnlineSdkEvent> =
        merge(setupFlowOnlineEvents, nonSetupFlowOnlineEvents)

    override val logEvents = _sdkEventFlow
        .filterIsInstance<SdkEvent.Internal.LogEvent>()
        .onEach {
            connectionStatus.first { it }
        }

    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("Register")
            logEventRegistry.logEvents
                .collect {
                    registerEvent(it)
                }
        }
    }

    override suspend fun registerEvent(sdkEvent: SdkEvent): SdkEventWaiterApi {
        return try {
            if (sdkEvent is OnlineSdkEvent) {
                eventsDao.insertEvent(sdkEvent)
            }
            _sdkEventFlow.emit(sdkEvent)
            SdkEventWaiter(this@SdkEventDistributor, sdkEvent, connectionStatus)
        } catch (exception: Exception) {
            currentCoroutineContext().ensureActive()
            sdkLogger.error(
                "SdkEventDistributor - Failed to register event",
                exception,
                buildJsonObject { put("event", sdkEvent.toString()) },
                isRemoteLog = sdkEvent !is SdkEvent.Internal.LogEvent
            )
            SdkEventWaiter(
                this@SdkEventDistributor,
                SdkEvent.Internal.Sdk.Answer.Response(
                    sdkEvent.id,
                    Result.failure<Exception>(exception)
                ),
                connectionStatus
            )
        }
    }

    override suspend fun emitEvent(sdkEvent: SdkEvent) {
        try {
            _sdkEventFlow.emit(sdkEvent)
        } catch (exception: Exception) {
            currentCoroutineContext().ensureActive()
            sdkLogger.error(
                "SdkEventDistributor - Failed to emit event",
                exception,
                buildJsonObject { put("event", sdkEvent.toString()) })
        }
    }
}