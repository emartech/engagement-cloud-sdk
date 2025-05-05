package com.emarsys.disable

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi

internal class DisableOrganizer(
    override val mobileEngageDisableStateMachine: StateMachineApi,
    override val predictDisableStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    private val sdkLogger: Logger
) : DisableOrganizerApi {

    override suspend fun disable() {
        sdkContext.setSdkState(SdkState.inactive)
        sdkLogger.debug("SDK disabled")

        if (sdkContext.isConfigPredictOnly()) {
            predictDisableStateMachine.activate()
        } else {
            mobileEngageDisableStateMachine.activate()
        }
    }
}