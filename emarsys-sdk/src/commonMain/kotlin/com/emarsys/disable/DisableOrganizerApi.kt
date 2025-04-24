package com.emarsys.disable

import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.StateMachineApi

interface DisableOrganizerApi {
    val meStateMachine: StateMachineApi
    val predictStateMachine: StateMachineApi
    val sdkContext: SdkContextApi

    suspend fun disable()
}