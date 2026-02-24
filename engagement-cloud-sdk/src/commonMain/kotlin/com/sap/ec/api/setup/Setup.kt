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
    override suspend fun enable(
        config: SdkConfig,
        onContactLinkingFailed: suspend () -> LinkContactData?
    ): Result<Unit> {
        return withContext(sdkContext.sdkDispatcher) {
            //todo check exception handling
            runCatchingWithoutCancellation {
                config.isValid(logger)
                sdkContext.onContactLinkingFailed = onContactLinkingFailed
                enableOrganizer.enableWithValidation(config)
            }
        }
    }

    override suspend fun disable(): Result<Unit> {
        return withContext(sdkContext.sdkDispatcher) {
            //todo check usage of SdkAlreadyDisabledException
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
        return sdkContext.config?.applicationCode != null
    }

    /**
     * Sets the setOnContactLinkingFailedCallback used to acquire contact data for contact linking.
     * @param onContactLinkingFailed The callback to be invoked when contact linking fails, allowing the app to provide contact data that the SDK can use to link.
     */
    override fun setOnContactLinkingFailedCallback(onContactLinkingFailed: suspend () -> LinkContactData?) {
        sdkContext.onContactLinkingFailed = onContactLinkingFailed
    }
}