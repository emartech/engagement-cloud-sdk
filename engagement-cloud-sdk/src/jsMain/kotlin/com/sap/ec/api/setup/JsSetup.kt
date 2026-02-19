package com.sap.ec.api.setup

import JsEngagementCloudSDKConfig
import com.sap.ec.JsApiConfig

internal class JsSetup(
    private val setup: SetupApi
) : JsSetupApi {

    /**
     * Enables the SDK with the provided configuration.
     * The SDK will start tracking only after this method has been called.
     *
     * @param config is the SDK configuration to use for enabling tracking.
     */
    override suspend fun enable(config: JsApiConfig) {
        setup.enable(
            JsEngagementCloudSDKConfig(
                applicationCode = config.applicationCode,
                config.serviceWorkerOptions
            )
        )
    }

    /**
     * Disables the SDK and it will no longer send or track any events.
     */
    override suspend fun disable() {
        setup.disable()
    }

    /**
     * Checks if the SDK is enabled.
     */
    override suspend fun isEnabled(): Boolean {
        return setup.isEnabled()
    }
}