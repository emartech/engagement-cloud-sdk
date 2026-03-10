package com.sap.ec.enable

import com.sap.ec.api.SdkState
import com.sap.ec.config.SdkConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException.SdkAlreadyEnabledException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.StateMachineApi
import com.sap.ec.enable.config.SdkConfigStoreApi
import com.sap.ec.mobileengage.session.SessionApi

internal class EnableOrganizer(
    override val meStateMachine: StateMachineApi,
    override val sdkContext: SdkContextApi,
    private val sdkConfigStore: SdkConfigStoreApi<SdkConfig>,
    private val ecSdkSession: SessionApi,
    private val sdkLogger: Logger
) : EnableOrganizerApi {

    override suspend fun enableWithValidation(config: SdkConfig) {
        if (sdkContext.isEnabledState()) {
            sdkLogger.debug("SDK already enabled")
            throw SdkAlreadyEnabledException("SAP Engagement Cloud SDK was already enabled!")
        }
        enable(config)
    }

    override suspend fun enable(config: SdkConfig) {
        sdkContext.setSdkState(SdkState.OnHold)
        sdkConfigStore.store(config)
        sdkContext.setSdkConfig(config)
        meStateMachine.activate()
            .onFailure {
                sdkContext.setSdkConfig(null)
                sdkConfigStore.clear()
                sdkContext.setSdkState(SdkState.Initialized)
                sdkLogger.debug("Enabling SDK failed during MeStateMachine activation. Failed with exception: ${it.message}")
            }.getOrThrow()
        sdkContext.setSdkState(SdkState.Active)
        ecSdkSession.startSession()
        sdkLogger.debug("Enabling SDK Completed")
    }
}