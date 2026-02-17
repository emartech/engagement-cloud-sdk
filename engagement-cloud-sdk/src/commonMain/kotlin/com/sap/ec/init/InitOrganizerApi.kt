package com.sap.ec.init

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.state.StateMachineApi

internal interface InitOrganizerApi {
    val initStateMachine: StateMachineApi
    val sdkContext: SdkContextApi

    suspend fun init()
}