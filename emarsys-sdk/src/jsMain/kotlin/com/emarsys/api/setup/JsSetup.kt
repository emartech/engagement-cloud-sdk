package com.emarsys.api.setup

import com.emarsys.config.SdkConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

internal class JsSetup(
    private val setup: SetupApi,
    private val applicationScope: CoroutineScope
) : JsSetupApi {

    /**
     * Enables tracking with the provided configuration.
     *
     * @param jsEmarsysConfig The SDK configuration to use for enabling tracking.
     * @return A promise that resolves when tracking is enabled.
     */
    override fun enableTracking(config: SdkConfig): Promise<Unit> {
        return applicationScope.promise {
            setup.enableTracking(config)
        }
    }

    /**
     * Disables tracking.
     *
     */
    override fun disableTracking(): Promise<Unit> {
        return applicationScope.promise {
            setup.disableTracking()
        }
    }
}