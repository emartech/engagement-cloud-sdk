package com.sap.ec.enable

import com.sap.ec.config.SdkConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.state.StateMachineApi

internal interface EnableOrganizerApi {
    val meStateMachine: StateMachineApi

    val sdkContext: SdkContextApi

    suspend fun enable(config: SdkConfig)

    suspend fun enableWithValidation(config: SdkConfig)
}