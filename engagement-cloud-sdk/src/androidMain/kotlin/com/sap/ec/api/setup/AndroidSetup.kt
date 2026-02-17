package com.sap.ec.api.setup

import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.core.exceptions.SdkException.SdkAlreadyEnabledException

class AndroidSetup(private val setup: SetupApi) : AndroidSetupApi {

    /**
     * Enables tracking with the provided [configuration][config].
     *
     * Example usage:
     * ```kotlin
     *         EngagementCloud.enableTracking(
     *             AndroidEngagementCloudSDKConfig(
     *                 applicationCode = "ABCDE-12345",
     *                 launchActivityClass = MyActivity::class.java,
     *             )
     *         )
     * ```
     *
     * @param config The SDK configuration to use for enabling tracking.
     * @throws SdkAlreadyEnabledException if tracking is already enabled.
     */
    override suspend fun enableTracking(config: AndroidEngagementCloudSDKConfig): Result<Unit> {
        return setup.enableTracking(config)
    }

    /**
     * Disables tracking.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyDisabledException if tracking is already disabled.
     */
    override suspend fun disableTracking(): Result<Unit> {
        return setup.disableTracking()
    }
    /**
     * Checks if tracking is enabled.
     * @returns a [Boolean] indicating if tracking is enabled,
     */
    override suspend fun isEnabled(): Boolean {
        return setup.isEnabled()
    }
}