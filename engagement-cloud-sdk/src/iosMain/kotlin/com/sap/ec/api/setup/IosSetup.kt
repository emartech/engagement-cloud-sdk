package com.sap.ec.api.setup

import com.sap.ec.IosEngagementCloudSDKConfig

class IosSetup(private val setup: SetupApi) : IosSetupApi {

    /**
     * Enables the SDK with the provided [config].
     * The SDK will start tracking only after this method has been called.
     *
     * Example usage:
     * ```Swift
     *         EngagementCloud.setup.enableTracking(
     *             EngagementCloudConfig(
     *                 applicationCode = "ABCDE-12345"
     *             )
     *         )
     * ```
     *
     * @param config The SDK configuration to use for enabling the SDK.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyEnabledException if tracking is already enabled.
     */
    override suspend fun enable(config: IosEngagementCloudSDKConfig) {
        setup.enable(config)
    }

    /**
     * Disables the SDK and it will no longer send or track any events.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyDisabledException if tracking is already disabled.
     */
    override suspend fun disable() {
        setup.disable()
    }
    /**
     * Checks if the SDK is enabled.
     * @returns a [Boolean] indicating if the SDK is enabled,
     */
    override suspend fun isEnabled(): Boolean {
        return setup.isEnabled()
    }
}