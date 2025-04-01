package com.emarsys.init.states

import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.setup.PlatformInitializerApi

internal class InitializerState(
    private val platformInitializer: PlatformInitializerApi,
    private val sdkLogger: Logger
) : State {
    override val name: String = "platformInitState"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkLogger.debug("PlatformInitState", "Initializing platforms")
        platformInitializer.init()
    }

    override fun relax() {
    }
}