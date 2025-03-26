package com.emarsys.setup

import com.emarsys.SdkConfig
import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.context.isConfigPredictOnly
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.setup.config.SdkConfigStoreApi

class SetupOrganizer(
    override val meStateMachine: StateMachineApi,
    override val predictStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    private val sdkConfigLoader: SdkConfigStoreApi<SdkConfig>,
    private val sdkLogger: Logger
) : SetupOrganizerApi {

    override suspend fun setupWithValidation(config: SdkConfig) {
        if (sdkConfigLoader.load() != null) {
            sdkLogger.debug("SetupOrganizer", "SDK already enabled")
            throw SdkAlreadyEnabledException("Emarsys SDK was already enabled!")
        }

        setup(config)
    }

    override suspend fun setup(config: SdkConfig) {
        sdkConfigLoader.store(config)
        sdkContext.config = config
        if (sdkContext.isConfigPredictOnly()) {
            predictStateMachine.activate()
        } else {
            meStateMachine.activate()
        }
        sdkContext.setSdkState(SdkState.active)
        sdkLogger.debug("SetupOrganizer", "SDK Setup Completed")
    }
}