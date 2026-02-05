package com.emarsys.enable.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent
import com.emarsys.response.mapToUnitOrFailure
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