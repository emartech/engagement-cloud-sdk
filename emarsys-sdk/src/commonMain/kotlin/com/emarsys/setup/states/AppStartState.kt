package com.emarsys.setup.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.state.State

import com.emarsys.networking.clients.event.model.SdkEvent

internal class AppStartState(
    private val sdkEventDistributor: SdkEventDistributorApi,
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
            sdkEventDistributor.registerAndStoreEvent(appStartEvent)
            alreadyCompleted = true
        }
    }

    override fun relax() {
    }
}