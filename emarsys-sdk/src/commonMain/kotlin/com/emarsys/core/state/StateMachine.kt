package com.emarsys.core.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StateMachine(private val states: List<State>): StateContext {

    private val innerStateLifecycle: MutableStateFlow<Pair<String, StateLifecycle>?> = MutableStateFlow(null)
    override val stateLifecycle: StateFlow<Pair<String, StateLifecycle>?> = innerStateLifecycle.asStateFlow()

    suspend fun activate() {
        states.forEach {
            innerStateLifecycle.value = it.name to StateLifecycle.prepare
            it.prepare()
            innerStateLifecycle.value = it.name to StateLifecycle.activate
            it.active()
            it.relax()
            innerStateLifecycle.value = it.name to StateLifecycle.relaxed
        }
    }

}
