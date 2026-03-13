package com.sap.ec.disable.states

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import com.sap.ec.response.mapToUnitOrFailure

internal class UnlinkContactState(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkContext: SdkContextApi,
    private val sdkLogger: Logger
) : State {
    override val name = "unlinkContactState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Register UnlinkContact event.")
        return sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.UnlinkContact(applicationCode = sdkContext.getSdkConfig()?.applicationCode))
            .await<Response>().mapToUnitOrFailure()
    }

    override fun relax() {}
}