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

    // TODO how to signal when auto-init fails
    override suspend fun init() {
        initStateMachine.activate()
            .onFailure {
                sdkLogger.debug("SDK initialized", it)
            }.getOrThrow()

        if (sdkContext.currentSdkState.value == SdkState.Inactive) {
            sdkContext.setSdkState(SdkState.Initialized)
        }
    }
}