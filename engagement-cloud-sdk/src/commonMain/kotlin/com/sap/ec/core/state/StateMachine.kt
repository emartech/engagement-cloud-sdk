package com.sap.ec.core.state

import com.sap.ec.core.log.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class StateMachine(
    private val states: List<State>,
    private val name: String,
    private val logger: Logger
) : StateMachineApi {
    private val innerStateLifecycle: MutableStateFlow<Pair<String, StateLifecycle>?> =
        MutableStateFlow(null)
    override val stateLifecycle: StateFlow<Pair<String, StateLifecycle>?> =
        innerStateLifecycle.asStateFlow()

    override suspend fun activate(): Result<Unit> {
        states.forEach {
            innerStateLifecycle.value = it.name to StateLifecycle.prepare
            it.prepare()
            innerStateLifecycle.value = it.name to StateLifecycle.activate
            logger.trace("$name machine activating state: ${it.name}")
            it.active()
                .onSuccess { _ ->
                    logger.trace("$name machine successfully activated state: ${it.name}")
                }.onFailure { throwable ->
                    logger.trace("$name machine failed to activate state: ${it.name} with error: $throwable")
                    return Result.failure(throwable)
                }
            it.relax()
            innerStateLifecycle.value = it.name to StateLifecycle.relaxed
        }
        return Result.success(Unit)
    }

}
