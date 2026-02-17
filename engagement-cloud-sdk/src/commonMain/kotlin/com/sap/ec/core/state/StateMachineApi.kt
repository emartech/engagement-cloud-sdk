package com.sap.ec.core.state

import kotlinx.coroutines.flow.StateFlow

internal interface StateMachineApi {
    val stateLifecycle: StateFlow<Pair<String, StateLifecycle>?>
    suspend fun activate(): Result<Unit>
}