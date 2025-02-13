package com.emarsys.core.setup

import com.emarsys.core.state.State

class PlatformInitState : State {
    override val name: String = "iOSInitState"
    override fun prepare() {}

    override suspend fun active() {}

    override fun relax() {}
}