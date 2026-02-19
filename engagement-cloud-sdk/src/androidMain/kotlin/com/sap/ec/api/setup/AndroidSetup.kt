package com.sap.ec.api.setup

import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.core.exceptions.SdkException.SdkAlreadyEnabledException

class AndroidSetup(private val setup: SetupApi) : AndroidSetupApi {

    /**
     * Enables the SDK the provided [config].
     * The SDK will start tracking only after this method has been called.
     *
     * Example usage:
     * ```kotlin
     *         EngagementCloud.enable(
     *             AndroidEngagementCloudSDKConfig(
     *                 applicationCode = "ABCDE-12345",
     *                 launchActivityClass = MyActivity::class.java,
     *             )
     *         )
     * ```
     *
     * @param config The SDK configuration to use for enabling the SDK.
     * @throws SdkAlreadyEnabledException if tracking is already enabled.
     */
    override suspend fun enable(config: AndroidEngagementCloudSDKConfig): Result<Unit> {
        return setup.enable(config)
    }

    /**
     * Disables the SDK and it will no longer send or track any events.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyDisabledException if tracking is already disabled.
     */
    override suspend fun disable(): Result<Unit> {
        return setup.disable()
    }
    /**
     * Checks if the SDK is enabled.
     * @returns a [Boolean] indicating if the SDK is enabled,
     */
    override suspend fun isEnabled(): Boolean {
        return setup.isEnabled()
    }
}