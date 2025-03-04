package com.emarsys.init.states

import com.emarsys.core.state.State
import com.emarsys.setup.PlatformInitializerApi

class PlatformInitState(private val platformInitializer: PlatformInitializerApi) : State {
    override val name: String = "platformInitState"

    override fun prepare() {
    }

    override suspend fun active() {
        platformInitializer.init()
    }

    override fun relax() {
    }
}