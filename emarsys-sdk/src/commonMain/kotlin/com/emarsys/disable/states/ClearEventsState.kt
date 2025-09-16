package com.emarsys.disable.states

import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.state.State
import com.emarsys.util.runCatchingWithoutCancellation

class ClearEventsState(private val eventsDao: EventsDaoApi) : State {
    override val name = "ClearEvents"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return runCatchingWithoutCancellation {
            eventsDao.removeAll()
        }
    }

    override fun relax() {
    }
}