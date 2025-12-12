package com.emarsys.api.setup

import JsEmarsysConfig
import com.emarsys.JsApiConfig
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
     * @param jSApiConfig The SDK configuration to use for enabling tracking.
     * @return A promise that resolves when tracking is enabled.
     */
    override fun enableTracking(config: JsApiConfig): Promise<Unit> {
        return applicationScope.promise {
            setup.enableTracking(
                JsEmarsysConfig(
                    applicationCode = config.applicationCode,
                    config.serviceWorkerOptions
                )
            )
        }
    }

    /**
     * Disables tracking.
     * @returns a [Promise] indicating the result of the operation,
     * containing SdkAlreadyDisabledException if tracking is already disabled.
     */
    override fun disableTracking(): Promise<Unit> {
        return applicationScope.promise {
            setup.disableTracking()
        }
    }

    /**
     * Checks if tracking is enabled.
     * @returns a [Promise] indicating if tracking is enabled,
     */
    override fun isEnabled(): Promise<Boolean> {
        return applicationScope.promise {
            setup.isEnabled()
        }
    }
}