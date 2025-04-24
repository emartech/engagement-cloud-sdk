package com.emarsys.enable.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.state.State
import com.emarsys.networking.clients.event.model.SdkEvent

internal class ApplyAppCodeBasedRemoteConfigState(private val sdkEventDistributor: SdkEventDistributorApi) :
    State {

    override val name = "applyAppCodeBasedRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig())?.await()
    }

    override fun relax() {
    }
}