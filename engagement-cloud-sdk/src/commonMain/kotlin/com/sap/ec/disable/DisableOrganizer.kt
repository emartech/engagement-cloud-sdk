package com.sap.ec.disable

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException.SdkAlreadyDisabledException
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
        sdkContext.setSdkState(SdkState.Initialized)
        sdkLogger.debug("SDK disabled")
        mobileEngageDisableStateMachine.activate().getOrThrow()
        ecSdkSession.endSession()
    }

    override suspend fun disableWithValidation() {
        if (!sdkContext.isEnabledState()) {
            sdkLogger.debug("SDK already disabled")
            throw SdkAlreadyDisabledException("SAP Engagement Cloud SDK was already disabled!")
        }
        disable()
    }
}