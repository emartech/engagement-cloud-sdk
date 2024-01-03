package com.emarsys.core.state

import kotlinx.coroutines.flow.StateFlow

interface StateContext {
    
    val stateLifecycle: StateFlow<Pair<String, StateLifecycle>?>

}