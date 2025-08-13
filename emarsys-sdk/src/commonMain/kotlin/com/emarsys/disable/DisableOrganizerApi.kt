package com.emarsys.disable

import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.StateMachineApi

internal interface DisableOrganizerApi {
    val mobileEngageDisableStateMachine: StateMachineApi
    val sdkContext: SdkContextApi

    suspend fun disable()
}