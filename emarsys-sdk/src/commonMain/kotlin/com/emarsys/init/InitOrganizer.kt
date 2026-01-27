package com.emarsys.init

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi

internal class InitOrganizer(
    override val initStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    private val sdkLogger: Logger

) : InitOrganizerApi {

    override suspend fun init() {
        if (sdkContext.currentSdkState.value == SdkState.UnInitialized) {
            initStateMachine.activate()
                .onSuccess {
                    sdkContext.setSdkState(SdkState.Initialized)
                }
                .onFailure {
                    sdkLogger.debug("SDK initialization failed.", it)
                }.getOrThrow()
        }
    }
}