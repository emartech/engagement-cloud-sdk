package com.emarsys.disable.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent

internal class ClearPushTokenOnDisableState(
    private val sdkEventDistributor: SdkEventDistributorApi
) : State {
    override val name: String = "clearPushTokenOnDisableState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.ClearPushToken()
        ).await<Response>()
            .result
            .mapCatching {}
    }

    override fun relax() {
    }
}