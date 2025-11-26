package com.emarsys.init.states

import com.emarsys.core.channel.SdkEventEmitterApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent
import com.emarsys.util.runCatchingWithoutCancellation
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