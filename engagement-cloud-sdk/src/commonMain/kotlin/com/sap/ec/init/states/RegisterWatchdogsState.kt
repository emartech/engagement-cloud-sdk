package com.sap.ec.init.states

import com.sap.ec.core.Registerable
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.util.runCatchingWithoutCancellation

internal class RegisterWatchdogsState(
    private val lifecycleWatchDog: Registerable,
    private val connectionWatchDog: Registerable,
    private val sdkLogger: Logger
) :
    State {
    override val name: String = "registerWatchdogsState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Registering watchdogs")

        return runCatchingWithoutCancellation {
            connectionWatchDog.register()
            lifecycleWatchDog.register()
        }
    }

    override fun relax() {
    }
}