package com.emarsys.setup.states

import com.emarsys.core.state.State
import com.emarsys.remoteConfig.RemoteConfigHandlerApi

class ApplyRemoteConfigState(private val remoteConfigHandler: RemoteConfigHandlerApi): State {

    override val name = "applyRemoteConfig"

    override fun prepare() {
    }

    override suspend fun active() {
        remoteConfigHandler.handle()
    }

    override fun relax() {
    }
}