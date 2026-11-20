package com.sap.ec.init.states

import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.remoteConfig.RemoteConfig
import com.sap.ec.remoteConfig.RemoteConfigHandlerApi
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ApplySDKDefaultRemoteConfigState(
    private val remoteConfigHandler: RemoteConfigHandlerApi,
    private val sdkLogger: Logger
) : State {

    override val name = "applySDKDefaultRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Applying SDK default remote config")
        remoteConfigHandler.handle(RemoteConfig.Default.SDK)
        return Result.success(Unit)
    }

    override fun relax() {
    }
}