package com.sap.ec.init.states

import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.remoteConfig.RemoteConfigResponse
import com.sap.ec.remoteConfig.RemoteConfigResponseHandlerApi
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ApplySDKDefaultRemoteConfigState(
    private val remoteConfigResponseHandler: RemoteConfigResponseHandlerApi,
    private val sdkLogger: Logger
) : State {

    override val name = "applySDKDefaultRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Applying SDK default remote config")
        remoteConfigResponseHandler.handle(RemoteConfigResponse.SDK_DEFAULT, true)
        return Result.success(Unit)
    }

    override fun relax() {
    }
}