package com.emarsys.api.setup

import com.emarsys.config.SdkConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

class JsSetup(
    private val setup: SetupApi,
    private val applicationScope: CoroutineScope
) : JsSetupApi {
    override fun enableTracking(config: SdkConfig): Promise<Unit> {
        return applicationScope.promise {
            setup.enableTracking(config)
        }
    }

    override fun disableTracking(): Promise<Unit> {
        return applicationScope.promise {
            setup.disableTracking()
        }
    }
}