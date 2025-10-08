package com.emarsys.init.states

import com.emarsys.core.Registerable
import com.emarsys.core.state.State

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