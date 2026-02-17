package com.sap.ec.api.setup

import JsEngagementCloudSDKConfig
import com.sap.ec.JsApiConfig

internal class JsSetup(
    private val setup: SetupApi
) : JsSetupApi {

    /**
     * Enables tracking with the provided configuration.
     *
     * @param config is the SDK configuration to use for enabling tracking.
     */
    override suspend fun enableTracking(config: JsApiConfig) {
        setup.enableTracking(
            JsEngagementCloudSDKConfig(
                applicationCode = config.applicationCode,
                config.serviceWorkerOptions
            )
        )
    }

    /**
     * Disables tracking.
     */
    override suspend fun disableTracking() {
        setup.disableTracking()
    }

    /**
     * Checks if tracking is enabled.
     */
    override suspend fun isEnabled(): Boolean {
        return setup.isEnabled()
    }
}