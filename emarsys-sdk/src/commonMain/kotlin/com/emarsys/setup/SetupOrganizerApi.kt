package com.emarsys.setup

import com.emarsys.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.StateMachineApi

interface SetupOrganizerApi {
    val meStateMachine: StateMachineApi
    val predictStateMachine: StateMachineApi
    val sdkContext: SdkContextApi

    suspend fun setup(config: SdkConfig)

    suspend fun setupWithValidation(config: SdkConfig)
}