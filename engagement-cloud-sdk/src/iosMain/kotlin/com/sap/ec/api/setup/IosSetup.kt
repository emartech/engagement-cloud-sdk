package com.sap.ec.api.setup

import com.sap.ec.IosEngagementCloudSDKConfig
import com.sap.ec.config.LinkContactData

class IosSetup(private val setup: SetupApi) : IosSetupApi {

    override suspend fun enable(
        config: IosEngagementCloudSDKConfig,
        onContactLinkingFailed: suspend () -> LinkContactData?
    ) {
        setup.enable(config, onContactLinkingFailed)
    }

    override suspend fun disable() {
        setup.disable()
    }

    override suspend fun isEnabled(): Boolean {
        return setup.isEnabled()
    }

    override fun setOnContactLinkingFailedCallback(onContactLinkingFailed: suspend () -> LinkContactData?) {
        setup.setOnContactLinkingFailedCallback(onContactLinkingFailed)
    }
}