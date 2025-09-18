package com.emarsys.api.setup

import com.emarsys.IosEmarsysConfig

class IosSetup(private val setup: SetupApi) : IosSetupApi {
    override suspend fun enableTracking(config: IosEmarsysConfig) {
        setup.enableTracking(config)
    }

    override suspend fun disableTracking() {
        setup.disableTracking()
    }
}