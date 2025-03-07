package com.emarsys.init

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.StateMachineApi

class InitOrganizer(
    override val initStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi
) : InitOrganizerApi {
    override suspend fun init() {
        initStateMachine.activate()
        sdkContext.setSdkState(SdkState.initialized)
    }

}