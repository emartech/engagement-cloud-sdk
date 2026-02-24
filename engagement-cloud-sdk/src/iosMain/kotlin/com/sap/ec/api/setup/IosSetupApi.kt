package com.sap.ec.api.setup

import com.sap.ec.IosEngagementCloudSDKConfig
import com.sap.ec.config.LinkContactData
import com.sap.ec.core.exceptions.SdkException.SdkAlreadyDisabledException
import com.sap.ec.core.exceptions.SdkException.SdkAlreadyEnabledException
import io.ktor.utils.io.CancellationException

interface IosSetupApi {

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
     * @param onContactLinkingFailed The callback to be invoked when contact linking fails, allowing the app to provide contact data that the SDK can use to link.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyEnabledException if tracking is already enabled.
     */
    @Throws(SdkAlreadyEnabledException::class, CancellationException::class)
    suspend fun enable(
        config: IosEngagementCloudSDKConfig,
        onContactLinkingFailed: suspend () -> LinkContactData?
    )

    /**
     * Disables the SDK and it will no longer send or track any events.
     * @returns a [Result] indicating the result of the operation,
     * containing SdkAlreadyDisabledException if tracking is already disabled.
     */
    @Throws(SdkAlreadyDisabledException::class, CancellationException::class)
    suspend fun disable()

    /**
     * Checks if the SDK is enabled.
     * @returns a [Boolean] indicating if the SDK is enabled,
     */
    suspend fun isEnabled(): Boolean

    /**
     * Sets the setOnContactLinkingFailedCallback used to acquire contact data for contact linking.
     * @param onContactLinkingFailed The callback to be invoked when contact linking fails, allowing the app to provide contact data that the SDK can use to link.
     */
    fun setOnContactLinkingFailedCallback(onContactLinkingFailed: suspend () -> LinkContactData?)
}