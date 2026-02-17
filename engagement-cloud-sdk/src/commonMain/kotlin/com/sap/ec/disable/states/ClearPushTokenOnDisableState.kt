package com.sap.ec.disable.states

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
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