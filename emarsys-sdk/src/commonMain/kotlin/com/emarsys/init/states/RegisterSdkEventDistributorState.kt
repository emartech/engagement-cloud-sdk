package com.emarsys.init.states

import com.emarsys.core.Registerable
import com.emarsys.core.state.State

class RegisterSdkEventDistributorState(
    private val sdkEventDistributor: Registerable
): State {
    override val name: String = "registerSdkEventDistributorState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        sdkEventDistributor.register()
        return Result.success(Unit)
    }

    override fun relax() {}
}