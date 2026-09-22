package com.sap.ec.init.states

import com.sap.ec.core.channel.OperationalEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ApplyGlobalRemoteConfigState(
    private val operationalEventDistributor: OperationalEventDistributorApi,
    private val sdkLogger: Logger
) : State {

    override val name = "applyGlobalRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Applying global remote config")
        operationalEventDistributor.registerOperationalEvent(SdkEvent.Internal.Sdk.ApplyGlobalRemoteConfig())
        return Result.success(Unit)
    }

    override fun relax() {
    }
}