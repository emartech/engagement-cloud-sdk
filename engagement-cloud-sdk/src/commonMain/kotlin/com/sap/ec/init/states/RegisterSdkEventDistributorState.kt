package com.sap.ec.init.states

import com.sap.ec.core.Registerable
import com.sap.ec.core.state.State

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