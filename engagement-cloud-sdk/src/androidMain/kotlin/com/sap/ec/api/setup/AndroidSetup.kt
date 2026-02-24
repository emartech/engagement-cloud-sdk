package com.sap.ec.api.setup

import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.config.LinkContactData

class AndroidSetup(private val setup: SetupApi) : AndroidSetupApi {

    override suspend fun enable(
        config: AndroidEngagementCloudSDKConfig,
        onContactLinkingFailed: suspend () -> LinkContactData?
    ): Result<Unit> {
        return setup.enable(config, onContactLinkingFailed)
    }

    override suspend fun disable(): Result<Unit> {
        return setup.disable()
    }


    override suspend fun isEnabled(): Boolean {
        return setup.isEnabled()
    }

    override fun setOnContactLinkingFailedCallback(onContactLinkingFailed: suspend () -> LinkContactData?) {
        setup.setOnContactLinkingFailedCallback(onContactLinkingFailed)
    }
}