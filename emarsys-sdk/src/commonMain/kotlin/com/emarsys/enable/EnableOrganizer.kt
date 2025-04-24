package com.emarsys.enable

import com.emarsys.SdkConfig
import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.context.isConfigPredictOnly
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.enable.config.SdkConfigStoreApi

class EnableOrganizer(
    override val meStateMachine: StateMachineApi,
    override val predictStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    private val sdkConfigStore: SdkConfigStoreApi<SdkConfig>,
    private val sdkLogger: Logger
) : EnableOrganizerApi {

    override suspend fun enableWithValidation(config: SdkConfig) {
        if (sdkConfigStore.load() != null) {
            sdkLogger.debug("SDK already enabled")
            throw SdkAlreadyEnabledException("Emarsys SDK was already enabled!")
        }
        enable(config)
    }

    override suspend fun enable(config: SdkConfig) {
        sdkContext.setSdkState(SdkState.onHold)
        sdkConfigStore.store(config)
        sdkContext.config = config
        if (sdkContext.isConfigPredictOnly()) {
            predictStateMachine.activate()
        } else {
            meStateMachine.activate()
        }
        sdkContext.setSdkState(SdkState.active)
        sdkLogger.debug("SDK Setup Completed")
    }
}