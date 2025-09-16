package com.emarsys.core.setup

import com.emarsys.core.state.State

internal class PlatformInitState : State {
    override val name: String = "iOSInitState"
    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        return Result.success(Unit)
    }

    override fun relax() {}
}