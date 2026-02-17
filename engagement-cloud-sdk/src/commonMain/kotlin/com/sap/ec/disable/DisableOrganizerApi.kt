package com.sap.ec.disable

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.state.StateMachineApi

internal interface DisableOrganizerApi {
    val mobileEngageDisableStateMachine: StateMachineApi
    val sdkContext: SdkContextApi

    suspend fun disable()
}