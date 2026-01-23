package com.emarsys.mobileengage.config

import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.StateMachineApi

internal interface FollowUpChangeAppCodeOrganizerApi {
    val followUpChangeAppCodeStateMachine: StateMachineApi
    val sdkContext: SdkContextApi

    suspend fun organize()
}