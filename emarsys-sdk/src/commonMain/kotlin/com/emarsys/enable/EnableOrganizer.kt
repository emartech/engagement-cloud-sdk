package com.emarsys.enable

import com.emarsys.api.SdkState
import com.emarsys.config.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.SdkException.SdkAlreadyEnabledException
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.enable.config.SdkConfigStoreApi
import com.emarsys.mobileengage.session.SessionApi

internal class EnableOrganizer(
    override val meStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    private val sdkConfigStore: SdkConfigStoreApi<SdkConfig>,
    private val emarsysSdkSession: SessionApi,
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
        sdkContext.setSdkState(SdkState.OnHold)
        sdkConfigStore.store(config)
        sdkContext.config = config
        meStateMachine.activate().getOrThrow()
        sdkContext.setSdkState(SdkState.Active)
        emarsysSdkSession.startSession()
        sdkLogger.debug("SDK Setup Completed")
    }
}