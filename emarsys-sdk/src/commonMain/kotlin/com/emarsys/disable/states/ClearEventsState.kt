package com.emarsys.disable.states

import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent
import com.emarsys.util.runCatchingWithoutCancellation
import kotlinx.coroutines.flow.filter

class ClearEventsState(private val eventsDao: EventsDaoApi) : State {
    override val name = "ClearEvents"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return runCatchingWithoutCancellation {
            eventsDao.getEvents()
                .filter {
                    it !is SdkEvent.Internal.LogEvent && it !is SdkEvent.Internal.OperationalEvent
                }.collect { event ->
                    eventsDao.removeEvent(event)
                }
        }
    }

    override fun relax() {
    }
}