package com.emarsys.setup.states

import com.emarsys.core.state.State
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import com.emarsys.providers.Provider
import kotlinx.datetime.Instant

class AppStartState(
    private val eventClient: EventClientApi,
    private val timestampProvider: Provider<Instant>
) : State {
    private var alreadyCompleted = false

    private companion object {
        const val APP_START_EVENT_NAME = "app:start"
    }

    override val name: String = "appStartState"
    override fun prepare() {
    }

    override suspend fun active() {
        if (!alreadyCompleted) {
            val appStartEvent = Event(
                EventType.INTERNAL,
                APP_START_EVENT_NAME,
                null,
                timestampProvider.provide().toString()
            )
            eventClient.registerEvent(appStartEvent)
            alreadyCompleted = true
        }
    }

    override fun relax() {
    }
}