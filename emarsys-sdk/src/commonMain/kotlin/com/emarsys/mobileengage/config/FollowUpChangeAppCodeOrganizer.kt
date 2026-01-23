package com.emarsys.mobileengage.config

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi

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