package com.emarsys.setup.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.state.State
import com.emarsys.networking.clients.event.model.SdkEvent

class ApplyAppCodeBasedRemoteConfigState(private val sdkEventDistributor: SdkEventDistributorApi) :
    State {

    override val name = "applyAppCodeBasedRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig())
    }

    override fun relax() {
    }
}