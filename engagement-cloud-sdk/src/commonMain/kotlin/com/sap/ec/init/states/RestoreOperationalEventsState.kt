package com.sap.ec.init.states

import com.sap.ec.core.channel.SdkEventEmitterApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import com.sap.ec.util.runCatchingWithoutCancellation
import kotlinx.coroutines.flow.filter

internal class RestoreOperationalEventsState(
    private val eventEmitter: SdkEventEmitterApi,
    private val eventsDao: EventsDaoApi
) : State {
    override val name = "RestoreOperationalEvents"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        return runCatchingWithoutCancellation {
            eventsDao.getEvents()
                .filter {
                    it is SdkEvent.Internal.LogEvent || it is SdkEvent.Internal.OperationalEvent
                }.collect { event ->
                    eventEmitter.emitEvent(event)
                }
        }
    }

    override fun relax() {}
}