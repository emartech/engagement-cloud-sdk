package com.emarsys.init.states

import com.emarsys.core.state.State
import com.emarsys.remoteConfig.RemoteConfigHandlerApi

class ApplyGlobalRemoteConfigState(private val remoteConfigHandler: RemoteConfigHandlerApi) : State {

    override val name = "applyGlobalRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active() {
        remoteConfigHandler.handleGlobal()
    }

    override fun relax() {
    }
}