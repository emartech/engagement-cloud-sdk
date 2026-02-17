package com.sap.ec.enable.states

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import com.sap.ec.response.mapToUnitOrFailure
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