package com.emarsys.disable.states

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)

internal class ClearPushTokenOnDisableState(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkContext: SdkContextApi
) : State {
    override val name: String = "clearPushTokenOnDisableState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.ClearPushToken(applicationCode = sdkContext.config?.applicationCode)
        ).await<Response>()
            .result
            .mapCatching {}
    }

    override fun relax() {
    }
}