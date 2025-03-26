package com.emarsys.setup.states

import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.state.State
import com.emarsys.networking.clients.event.EventClientApi

import com.emarsys.networking.clients.event.model.SdkEvent

internal class AppStartState(
    private val eventClient: EventClientApi,
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi
) : State {
    private var alreadyCompleted = false

    override val name: String = "appStartState"
    override fun prepare() {
    }

    override suspend fun active() {
        if (!alreadyCompleted) {
            val appStartEvent = SdkEvent.Internal.Sdk.AppStart(
                id = uuidProvider.provide(),
                timestamp = timestampProvider.provide()
            )
            eventClient.registerEvent(appStartEvent)
            alreadyCompleted = true
        }
    }

    override fun relax() {
    }
}