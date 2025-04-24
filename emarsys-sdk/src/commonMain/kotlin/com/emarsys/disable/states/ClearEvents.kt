package com.emarsys.disable.states

import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.state.State

class ClearEvents(private val eventsDao: EventsDaoApi) : State {
    override val name = "ClearEvents"

    override fun prepare() {
    }

    override suspend fun active() {
        eventsDao.removeAll()

    }

    override fun relax() {
    }
}