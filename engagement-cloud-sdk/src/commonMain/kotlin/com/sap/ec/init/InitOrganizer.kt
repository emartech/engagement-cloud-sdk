package com.sap.ec.init

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.StateMachineApi

internal class InitOrganizer(
    override val initStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    private val sdkLogger: Logger

) : InitOrganizerApi {

    override suspend fun init() {
        if (sdkContext.currentSdkState.value == SdkState.UnInitialized) {
            initStateMachine.activate()
                .onSuccess {
                    if (sdkContext.currentSdkState.value == SdkState.UnInitialized) {
                        sdkContext.setSdkState(SdkState.Initialized)
                    }
                }
                .onFailure {
                    sdkLogger.debug("SDK initialization failed.", it)
                }.getOrThrow()
        }
    }
}