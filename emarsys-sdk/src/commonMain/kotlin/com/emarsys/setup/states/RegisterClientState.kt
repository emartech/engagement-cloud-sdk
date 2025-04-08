package com.emarsys.setup.states

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
        sdkEventDistributor.registerAndStoreEvent(SdkEvent.Internal.Sdk.RegisterDeviceInfo())
    }

    override fun relax() {
    }
}