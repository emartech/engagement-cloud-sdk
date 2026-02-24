package com.sap.ec.reregistration.states

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class LinkContactState(
    private val sdkContext: SdkContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger
) : State {
    override val name = "linkContactState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Linking contact")
        return Result.success(Unit)
    }


    override fun relax() {}
}