package com.emarsys.init.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ApplyGlobalRemoteConfigState(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger
) : State {

    override val name = "applyGlobalRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkLogger.debug("Applying global remote config")
        sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.ApplyGlobalRemoteConfig())
    }

    override fun relax() {
    }
}