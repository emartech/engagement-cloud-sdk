package com.emarsys.api.setup

import com.emarsys.IosEmarsysConfig

class IosSetup(private val setup: SetupApi) : IosSetupApi {

    /**
     * Enables tracking with the provided [configuration][config].
     *
     * Example usage:
     * ```Swift
     *         Emarsys.setup.enableTracking(
     *             IosEmarsysConfig(
     *                 applicationCode = "ABCDE-12345"
     *             )
     *         )
     * ```
     *
     * @param config The SDK configuration to use for enabling tracking.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyEnabledException if tracking is already enabled.
     */
    override suspend fun enableTracking(config: IosEmarsysConfig) {
        setup.enableTracking(config)
    }

    /**
     * Disables tracking.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyDisabledException if tracking is already disabled.
     */
    override suspend fun disableTracking() {
        setup.disableTracking()
    }
    /**
     * Checks if tracking is enabled.
     * @returns a [Boolean] indicating if tracking is enabled,
     */
    override suspend fun isEnabled(): Boolean {
        return setup.isEnabled()
    }
}