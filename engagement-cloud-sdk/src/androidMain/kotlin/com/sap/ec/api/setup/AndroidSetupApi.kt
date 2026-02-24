package com.sap.ec.api.setup

import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.config.LinkContactData
import com.sap.ec.core.exceptions.SdkException.SdkAlreadyEnabledException

interface AndroidSetupApi {

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
     * @param onContactLinkingFailed The callback to be invoked when contact linking fails, allowing the app to provide contact data that the SDK can use to link.
     * @throws SdkAlreadyEnabledException if tracking is already enabled.
     */
    suspend fun enable(
        config: AndroidEngagementCloudSDKConfig,
        onContactLinkingFailed: suspend () -> LinkContactData?
    ): Result<Unit>

    /**
     * Disables the SDK and it will no longer send or track any events.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyDisabledException if tracking is already disabled.
     */
    suspend fun disable(): Result<Unit>

    /**
     * Checks if the SDK is enabled.
     * @returns a [Boolean] indicating if the SDK is enabled,
     */
    suspend fun isEnabled(): Boolean

    /**
     * Sets the setOnContactLinkingFailedCallback used to acquire contact data for contact linking.
     * @param onContactLinkingFailed The callback to be invoked when contact linking fails, allowing the app to provide contact data that the SDK can use to link.
     */
    fun setOnContactLinkingFailedCallback(onContactLinkingFailed: (suspend () -> LinkContactData?))
}