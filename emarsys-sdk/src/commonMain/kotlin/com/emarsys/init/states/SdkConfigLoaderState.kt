package com.emarsys.init.states

import com.emarsys.SdkConfig
import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.setup.SetupOrganizerApi
import com.emarsys.setup.config.SdkConfigStoreApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

class SdkConfigLoaderState(
    private val sdkConfigStore: SdkConfigStoreApi<SdkConfig>,
    private val setupOrganizer: SetupOrganizerApi,
    private val applicationScope: CoroutineScope,
    private val sdkLogger: Logger
) : State {
    override val name = "sdkConfigLoader"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkLogger.debug(
            "Load SdkConfig from storage and try to setup the SDK"
        )
        sdkConfigStore.load()?.let {
            applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
                setupOrganizer.setup(it)
            }
        }
    }

    override fun relax() {
    }
}