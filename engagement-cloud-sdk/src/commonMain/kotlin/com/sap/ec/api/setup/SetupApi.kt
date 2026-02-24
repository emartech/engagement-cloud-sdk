package com.sap.ec.api.setup

import com.sap.ec.config.LinkContactData
import com.sap.ec.config.SdkConfig

interface SetupApi {

    /**
     * Enables the SDK with the provided configuration.
     * The SDK will start tracking only after this method has been called.
     *
     * @param config is the SDK configuration to use for enabling tracking.
     * @param onContactLinkingFailed The callback to be invoked when contact linking fails, allowing the app to provide contact data that the SDK can use to link.
     */
    suspend fun enable(
        config: SdkConfig,
        onContactLinkingFailed: suspend () -> LinkContactData?
    ): Result<Unit>

    /**
     * Disables the SDK and it will no longer send or track any events.
     */
    suspend fun disable(): Result<Unit>

    /**
     * Checks if tracking is enabled.
     * @returns a [Boolean] indicating if tracking is enabled,
     */
    suspend fun isEnabled(): Boolean

    /**
     * Sets the setOnContactLinkingFailedCallback used to acquire contact data for contact linking.
     * @param onContactLinkingFailed The callback to be invoked when contact linking fails, allowing the app to provide contact data that the SDK can use to link.
     */
    fun setOnContactLinkingFailedCallback(onContactLinkingFailed: suspend () -> LinkContactData?)
}