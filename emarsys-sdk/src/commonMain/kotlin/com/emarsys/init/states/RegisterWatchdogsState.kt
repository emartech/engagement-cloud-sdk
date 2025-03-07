package com.emarsys.init.states

import com.emarsys.core.Registerable
import com.emarsys.core.state.State

class RegisterWatchdogsState(
    private val lifecycleWatchDog: Registerable,
    private val connectionWatchDog: Registerable
) :
    State {
    override val name: String = "registerWatchdogsState"

    override fun prepare() {
    }

    override suspend fun active() {
        connectionWatchDog.register()
        lifecycleWatchDog.register()
    }

    override fun relax() {
    }
}