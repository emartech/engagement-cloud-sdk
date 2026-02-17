package com.sap.ec.init.states

import com.sap.ec.config.SdkConfig
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.enable.EnableOrganizerApi
import com.sap.ec.enable.config.SdkConfigStoreApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

internal class SdkConfigLoaderState(
    private val sdkConfigStore: SdkConfigStoreApi<SdkConfig>,
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
        sdkConfigStore.load()?.let {
            applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
                setupOrganizer.enable(it)
            }
        }
        return Result.success(Unit)
    }

    override fun relax() {
    }
}