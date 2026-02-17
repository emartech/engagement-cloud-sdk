package com.sap.ec.enable

import com.sap.ec.core.state.State

internal class PlatformInitState : State {

    override val name: String = "androidInitState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        return Result.success(Unit)
    }

    override fun relax() {
    }
}