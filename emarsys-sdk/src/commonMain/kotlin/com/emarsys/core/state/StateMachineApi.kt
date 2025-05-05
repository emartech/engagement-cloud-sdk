package com.emarsys.core.state

import kotlinx.coroutines.flow.StateFlow

internal interface StateMachineApi {
    val stateLifecycle: StateFlow<Pair<String, StateLifecycle>?>
    suspend fun activate()
}