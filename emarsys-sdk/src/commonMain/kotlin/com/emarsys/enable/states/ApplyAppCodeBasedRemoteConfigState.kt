package com.emarsys.enable.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ApplyAppCodeBasedRemoteConfigState(private val sdkEventDistributor: SdkEventDistributorApi) :
    State {

    override val name = "applyAppCodeBasedRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig())
            .await<SdkEvent.Internal.Sdk.Answer.Response<Response>>()
    }

    override fun relax() {
    }
}