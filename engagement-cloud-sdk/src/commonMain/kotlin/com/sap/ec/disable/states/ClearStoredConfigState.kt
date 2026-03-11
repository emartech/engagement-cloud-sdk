package com.sap.ec.disable.states

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.util.runCatchingWithoutCancellation

internal class ClearStoredConfigState(
    private val sdkContext: SdkContextApi,
    private val sdkLogger: Logger
) : State {
    override val name = "ClearStoredConfig"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return runCatchingWithoutCancellation {
            sdkContext.setSdkConfig(null)
            sdkLogger.debug("Cleared stored config")
        }
    }

    override fun relax() {
    }
}