package com.emarsys.disable

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.mobileengage.session.SessionApi

internal class DisableOrganizer(
    override val mobileEngageDisableStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    private val emarsysSdkSession: SessionApi,
    private val sdkLogger: Logger
) : DisableOrganizerApi {

    override suspend fun disable() {
        sdkContext.setSdkState(SdkState.inactive)
        sdkLogger.debug("SDK disabled")
        mobileEngageDisableStateMachine.activate().getOrThrow()
        emarsysSdkSession.endSession()
    }
}