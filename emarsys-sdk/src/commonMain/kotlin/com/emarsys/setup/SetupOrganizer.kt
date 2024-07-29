package com.emarsys.setup

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.context.isConfigPredictOnly
import com.emarsys.core.state.StateMachineApi

class SetupOrganizer(
    override val meStateMachine: StateMachineApi,
    override val predictStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi
) : SetupOrganizerApi {

    override suspend fun setup(config: EmarsysConfig) {
        sdkContext.config = config
        if (sdkContext.isConfigPredictOnly()) {
            predictStateMachine.activate()
        } else {
            meStateMachine.activate()
        }
        sdkContext.setSdkState(SdkState.active)
    }
}