package com.emarsys.init.states

import com.emarsys.core.Registerable
import com.emarsys.core.log.Logger
import com.emarsys.core.state.State

class RegisterWatchdogsState(
    private val lifecycleWatchDog: Registerable,
    private val connectionWatchDog: Registerable,
    private val eventDistributor: Registerable,
    private val sdkLogger: Logger
) :
    State {
    override val name: String = "registerWatchdogsState"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkLogger.debug("RegisterWatchdogsState", "Registering watchdogs")

        connectionWatchDog.register()
        lifecycleWatchDog.register()
        eventDistributor.register()
    }

    override fun relax() {
    }
}