package com.emarsys.api.setup

import JsEmarsysConfig
import com.emarsys.JsApiConfig

internal class JsSetup(
    private val setup: SetupApi
) : JsSetupApi {

    /**
     * Enables tracking with the provided configuration.
     *
     * @param jSApiConfig The SDK configuration to use for enabling tracking.
     */
    override suspend fun enableTracking(config: JsApiConfig) {
        setup.enableTracking(
            JsEmarsysConfig(
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