package com.sap.ec.disable.states

import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import com.sap.ec.util.runCatchingWithoutCancellation
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