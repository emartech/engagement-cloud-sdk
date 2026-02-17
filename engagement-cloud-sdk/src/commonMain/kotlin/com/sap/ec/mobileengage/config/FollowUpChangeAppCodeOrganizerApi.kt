package com.sap.ec.mobileengage.config

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.state.StateMachineApi

internal interface FollowUpChangeAppCodeOrganizerApi {
    val followUpChangeAppCodeStateMachine: StateMachineApi
    val sdkContext: SdkContextApi

    suspend fun organize()
}