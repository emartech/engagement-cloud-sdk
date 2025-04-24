package com.emarsys.enable.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.state.State
import com.emarsys.networking.clients.event.model.SdkEvent

internal class RegisterClientState(
    private val sdkEventDistributor: SdkEventDistributorApi
) : State {
    override val name: String
        get() = "registerClientState"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.RegisterDeviceInfo())?.await()
    }

    override fun relax() {
    }
}