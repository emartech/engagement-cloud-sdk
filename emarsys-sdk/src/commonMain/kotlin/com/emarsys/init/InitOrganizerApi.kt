package com.emarsys.init

import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.StateMachineApi

internal interface InitOrganizerApi {
    val initStateMachine: StateMachineApi
    val sdkContext: SdkContextApi

    suspend fun init()
}