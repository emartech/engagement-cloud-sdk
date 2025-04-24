package com.emarsys.enable

import com.emarsys.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.StateMachineApi

interface EnableOrganizerApi {
    val meStateMachine: StateMachineApi
    val predictStateMachine: StateMachineApi
    val sdkContext: SdkContextApi

    suspend fun enable(config: SdkConfig)

    suspend fun enableWithValidation(config: SdkConfig)
}