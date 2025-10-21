package com.emarsys.api.setup

import com.emarsys.IosEmarsysConfig

class IosSetup(private val setup: SetupApi) : IosSetupApi {

    /**
     * Enables tracking with the provided configuration.
     *
     * @param config The SDK configuration to use for enabling tracking.
     */
    override suspend fun enableTracking(config: IosEmarsysConfig) {
        setup.enableTracking(config)
    }

    /**
     * Disables tracking.
     */
    override suspend fun disableTracking() {
        setup.disableTracking()
    }
}