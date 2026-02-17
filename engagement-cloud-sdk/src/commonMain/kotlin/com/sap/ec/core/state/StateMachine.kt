package com.sap.ec.core.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class StateMachine(private val states: List<State>) : StateMachineApi {
    private val innerStateLifecycle: MutableStateFlow<Pair<String, StateLifecycle>?> =
        MutableStateFlow(null)
    override val stateLifecycle: StateFlow<Pair<String, StateLifecycle>?> =
        innerStateLifecycle.asStateFlow()

    override suspend fun activate(): Result<Unit> {
        states.forEach {
            innerStateLifecycle.value = it.name to StateLifecycle.prepare
            it.prepare()
            innerStateLifecycle.value = it.name to StateLifecycle.activate
            it.active().onFailure { throwable ->
                return Result.failure(throwable)
            }
            it.relax()
            innerStateLifecycle.value = it.name to StateLifecycle.relaxed
        }
        return Result.success(Unit)
    }

}
