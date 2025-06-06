package com.emarsys.disable.states

import com.emarsys.config.SdkConfig
import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.enable.config.SdkConfigStoreApi

internal class ClearStoredConfig(
    private val sdkConfigStore: SdkConfigStoreApi<SdkConfig>,
    private val sdkLogger: Logger
) : State {
    override val name = "ClearStoredConfig"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkConfigStore.clear()
        sdkLogger.debug("Cleared stored config")
    }

    override fun relax() {
    }
}