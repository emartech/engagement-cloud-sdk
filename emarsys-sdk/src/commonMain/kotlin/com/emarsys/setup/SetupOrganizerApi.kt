package com.emarsys.setup

import com.emarsys.EmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.StateMachineApi

interface SetupOrganizerApi {
    val stateMachine: StateMachineApi
    val sdkContext: SdkContextApi

    suspend fun setup(config: EmarsysConfig)
}