package com.sap.ec.init.states

import com.sap.ec.core.Registerable
import com.sap.ec.core.state.State

internal class RegisterEventConsumersState(
    private val consumers: List<Registerable>
) : State {
    override val name: String = "registerEventConsumersState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        consumers.forEach { it.register() }
        return Result.success(Unit)
    }

    override fun relax() {}

}