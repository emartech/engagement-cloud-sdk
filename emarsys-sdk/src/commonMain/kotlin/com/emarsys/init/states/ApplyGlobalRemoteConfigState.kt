package com.emarsys.init.states

import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.remoteConfig.RemoteConfigHandlerApi

class ApplyGlobalRemoteConfigState(private val remoteConfigHandler: RemoteConfigHandlerApi, private val sdkLogger: Logger) : State {

    override val name = "applyGlobalRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkLogger.debug("Applying global remote config")
        remoteConfigHandler.handleGlobal()
    }

    override fun relax() {
    }
}