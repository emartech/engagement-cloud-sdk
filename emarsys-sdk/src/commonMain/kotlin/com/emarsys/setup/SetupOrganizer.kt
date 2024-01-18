package com.emarsys.setup

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.StateMachineApi

class SetupOrganizer(
    override val stateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi
) : SetupOrganizerApi {

    override suspend fun setup(config: EmarsysConfig) {
        sdkContext.config = config
        stateMachine.activate()
        sdkContext.setSdkState(SdkState.active)
    }
}