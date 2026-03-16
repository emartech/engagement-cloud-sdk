package com.sap.ec.api.setup

import com.sap.ec.config.LinkContactData
import com.sap.ec.config.SdkConfig
import com.sap.ec.config.isValid
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.disable.DisableOrganizerApi
import com.sap.ec.enable.EnableOrganizerApi
import com.sap.ec.util.runCatchingWithoutCancellation
import kotlinx.coroutines.withContext

internal class Setup(
    private val enableOrganizer: EnableOrganizerApi,
    private val disableOrganizer: DisableOrganizerApi,
    private val sdkContext: SdkContextApi,
    private val logger: Logger
) : SetupApi {
    /**
     * Enables the SDK with the provided configuration.
     * @param config The configuration for the SDK.
     */
    override suspend fun enable(
        config: SdkConfig,
        onContactLinkingFailed: suspend () -> LinkContactData?
    ): Result<Unit> {
        return withContext(sdkContext.sdkDispatcher) {
            runCatchingWithoutCancellation {
                config.isValid(logger)
                sdkContext.onContactLinkingFailed = onContactLinkingFailed
                enableOrganizer.enableWithValidation(config)
            }
        }
    }

    /**
     * Disables the SDK.
     * @returns a [Result] indicating the success or failure of the operation.
     */
    override suspend fun disable(): Result<Unit> {
        return withContext(sdkContext.sdkDispatcher) {
            runCatchingWithoutCancellation {
                disableOrganizer.disable()
            }
        }
    }

    /**
     * Checks if tracking is enabled.
     * @returns a [Boolean] indicating if tracking is enabled,
     */
    override suspend fun isEnabled(): Boolean {
        return sdkContext.getSdkConfig()?.applicationCode != null
    }

    /**
     * Sets the setOnContactLinkingFailedCallback used to acquire contact data for contact linking.
     * @param onContactLinkingFailed The callback to be invoked when contact linking fails, allowing the app to provide contact data that the SDK can use to link.
     */
    override fun setOnContactLinkingFailedCallback(onContactLinkingFailed: suspend () -> LinkContactData?) {
        sdkContext.onContactLinkingFailed = onContactLinkingFailed
    }
}