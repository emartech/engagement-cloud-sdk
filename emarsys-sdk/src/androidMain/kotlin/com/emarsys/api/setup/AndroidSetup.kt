package com.emarsys.api.setup

import com.emarsys.AndroidEmarsysConfig
import com.emarsys.core.exceptions.SdkException.SdkAlreadyEnabledException

class AndroidSetup(private val setup: SetupApi) : AndroidSetupApi {

    /**
     * Enables tracking with the provided [configuration][config].
     *
     * Example usage:
     * ```kotlin
     *         AndroidEmarsys.enableTracking(
     *             AndroidEmarsysConfig(
     *                 applicationCode = "ABCDE-12345",
     *                 launchActivityClass = MyActivity::class.java,
     *             )
     *         )
     * ```
     *
     * @param config The SDK configuration to use for enabling tracking.
     * @throws SdkAlreadyEnabledException if tracking is already enabled.
     */
    override suspend fun enableTracking(config: AndroidEmarsysConfig): Result<Unit> {
        return setup.enableTracking(config)
    }

    /**
     * Disables tracking.
     */
    override suspend fun disableTracking(): Result<Unit> {
        return setup.disableTracking()
    }
}