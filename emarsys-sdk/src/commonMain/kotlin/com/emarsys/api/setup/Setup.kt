package com.emarsys.api.setup

import com.emarsys.config.SdkConfig
import com.emarsys.config.isValid
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.disable.DisableOrganizerApi
import com.emarsys.enable.EnableOrganizerApi
import com.emarsys.util.runCatchingWithoutCancellation
import kotlinx.coroutines.withContext

internal class Setup(
    private val enableOrganizer: EnableOrganizerApi,
    private val disableOrganizer: DisableOrganizerApi,
    private val sdkContext: SdkContextApi,
    private val logger: Logger
) : SetupApi {
    override suspend fun enableTracking(config: SdkConfig): Result<Unit> {
        return withContext(sdkContext.sdkDispatcher) {
            //todo check exception handling
            runCatchingWithoutCancellation {
                config.isValid(logger)
                enableOrganizer.enableWithValidation(config)
            }
        }
    }

    override suspend fun disableTracking(): Result<Unit> {
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
}