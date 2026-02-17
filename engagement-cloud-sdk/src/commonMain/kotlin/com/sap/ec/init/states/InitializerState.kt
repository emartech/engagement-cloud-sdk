package com.sap.ec.init.states

import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.enable.PlatformInitializerApi
import com.sap.ec.util.runCatchingWithoutCancellation

internal class InitializerState(
    private val platformInitializer: PlatformInitializerApi,
    private val sdkLogger: Logger
) : State {
    override val name: String = "platformInitState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return runCatchingWithoutCancellation {
            sdkLogger.debug("Initializing platforms")
            platformInitializer.init()
        }
    }

    override fun relax() {
    }
}