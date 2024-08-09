package com.emarsys.setup

import com.emarsys.core.state.State
import com.emarsys.mobileengage.inapp.InappJsBridgeApi

class PlatformInitState(
    private val inappJsBridge: InappJsBridgeApi
) : State {

    override val name: String = "jsInitState"

    override fun prepare() {

    }

    override suspend fun active() {
        inappJsBridge.register()
    }

    override fun relax() {
    }
}