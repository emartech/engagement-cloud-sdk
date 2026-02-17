package com.sap.ec.disable

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.StateMachineApi
import com.sap.ec.mobileengage.session.SessionApi

internal class DisableOrganizer(
    override val mobileEngageDisableStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    private val ecSdkSession: SessionApi,
    private val sdkLogger: Logger
) : DisableOrganizerApi {

    override suspend fun disable() {
        sdkContext.setSdkState(SdkState.UnInitialized)
        sdkLogger.debug("SDK disabled")
        mobileEngageDisableStateMachine.activate().getOrThrow()
        ecSdkSession.endSession()
    }
}