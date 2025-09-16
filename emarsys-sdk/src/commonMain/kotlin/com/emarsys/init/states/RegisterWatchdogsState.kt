package com.emarsys.init.states

import com.emarsys.core.Registerable
import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.util.runCatchingWithoutCancellation

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