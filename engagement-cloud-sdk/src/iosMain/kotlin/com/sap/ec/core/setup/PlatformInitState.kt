package com.sap.ec.core.setup

import com.sap.ec.core.state.State

internal class PlatformInitState : State {
    override val name: String = "iOSInitState"
    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        return Result.success(Unit)
    }

    override fun relax() {}
}