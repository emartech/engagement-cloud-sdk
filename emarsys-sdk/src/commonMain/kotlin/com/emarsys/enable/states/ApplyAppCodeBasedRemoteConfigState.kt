package com.emarsys.enable.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent
import com.emarsys.response.mapToUnitOrFailure
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ApplyAppCodeBasedRemoteConfigState(private val sdkEventDistributor: SdkEventDistributorApi) :
    State {

    override val name = "applyAppCodeBasedRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig())
            .await<SdkEvent.Internal.Sdk.Answer.Response<Response>>()
            .mapToUnitOrFailure()
    }

    override fun relax() {
    }
}