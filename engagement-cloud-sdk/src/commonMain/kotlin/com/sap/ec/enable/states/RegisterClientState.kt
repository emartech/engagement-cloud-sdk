package com.sap.ec.enable.states

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import com.sap.ec.response.mapToUnitOrFailure
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class RegisterClientState(
    private val sdkEventDistributor: SdkEventDistributorApi
) : State {
    override val name: String
        get() = "registerClientState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.RegisterDeviceInfo())
            .await<Unit>()
            .mapToUnitOrFailure()
    }

    override fun relax() {
    }
}