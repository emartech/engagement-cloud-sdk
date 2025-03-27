package com.emarsys.init

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi

class InitOrganizer(
    override val initStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    private val sdkLogger: Logger

) : InitOrganizerApi {
    override suspend fun init() {
        initStateMachine.activate()
        if (sdkContext.currentSdkState.value == SdkState.inactive) {
            sdkContext.setSdkState(SdkState.initialized)
        }
        sdkLogger.debug("InitOrganizer", "SDK initialized")
    }

}