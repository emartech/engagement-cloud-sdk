package com.sap.ec.mobileengage.config

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.StateMachineApi

internal class FollowUpChangeAppCodeOrganizer(
    override val followUpChangeAppCodeStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    val logger: Logger
) : FollowUpChangeAppCodeOrganizerApi {

    override suspend fun organize() {
        sdkContext.setSdkState(SdkState.OnHold)
        followUpChangeAppCodeStateMachine.activate()
            .onFailure {
                logger.error(
                    "Failed to activate ChangeAppCodeStateMachine during app code change",
                    it
                )
            }
        sdkContext.setSdkState(SdkState.Active)
    }
}