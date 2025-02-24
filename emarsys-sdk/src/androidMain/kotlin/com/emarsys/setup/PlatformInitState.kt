package com.emarsys.setup

import com.emarsys.core.state.State

class PlatformInitState : State {

    override val name: String = "androidInitState"

    override fun prepare() {}

    override suspend fun active() {
    }

    override fun relax() {
    }
}