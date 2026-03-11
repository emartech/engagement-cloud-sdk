package com.sap.ec.init.states

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.enable.EnableOrganizerApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

internal class SdkConfigLoaderState(
    private val sdkContext: SdkContextApi,
    private val setupOrganizer: EnableOrganizerApi,
    private val applicationScope: CoroutineScope,
    private val sdkLogger: Logger
) : State {
    override val name = "sdkConfigLoader"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug(
            "Load SdkConfig from storage and try to setup the SDK"
        )
        sdkContext.getSdkConfig()?.let {
            applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
                try {
                    setupOrganizer.enable(it)
                } catch (exception: Exception) {
                    sdkLogger.debug("Failed to setup SDK with loaded config", exception)
                }
            }
        }
        return Result.success(Unit)
    }

    override fun relax() {
    }
}