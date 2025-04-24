package com.emarsys.enable

import com.emarsys.core.state.State

internal class PlatformInitState : State {

    override val name: String = "androidInitState"

    override fun prepare() {}

    override suspend fun active() {
    }

    override fun relax() {
    }
}