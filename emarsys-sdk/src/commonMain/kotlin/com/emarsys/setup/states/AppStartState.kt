package com.emarsys.setup.states

import com.emarsys.core.providers.Provider
import com.emarsys.core.state.State
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import kotlinx.datetime.Instant

class AppStartState(
    private val eventClient: EventClientApi,
    private val timestampProvider: Provider<Instant>
) : State {
    private var alreadyCompleted = false

    override val name: String = "appStartState"
    override fun prepare() {
    }

    override suspend fun active() {
        if (!alreadyCompleted) {
            val appStartEvent = Event.createAppStart(timestampProvider.provide())
            eventClient.registerEvent(appStartEvent)
            alreadyCompleted = true
        }
    }

    override fun relax() {
    }
}