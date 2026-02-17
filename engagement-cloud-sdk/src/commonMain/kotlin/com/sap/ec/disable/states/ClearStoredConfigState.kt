package com.sap.ec.disable.states

import com.sap.ec.config.SdkConfig
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.enable.config.SdkConfigStoreApi
import com.sap.ec.util.runCatchingWithoutCancellation

internal class ClearStoredConfigState(
    private val sdkConfigStore: SdkConfigStoreApi<SdkConfig>,
    private val sdkLogger: Logger
) : State {
    override val name = "ClearStoredConfig"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return runCatchingWithoutCancellation {
            sdkConfigStore.clear()
            sdkLogger.debug("Cleared stored config")
        }
    }

    override fun relax() {
    }
}