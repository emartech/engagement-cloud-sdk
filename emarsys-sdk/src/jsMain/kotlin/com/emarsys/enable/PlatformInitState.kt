package com.emarsys.enable

import JsEmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.State
import com.emarsys.mobileengage.push.PushServiceApi
import com.emarsys.util.runCatchingWithoutCancellation

internal class PlatformInitState(
    private val pushService: PushServiceApi,
    val sdkContext: SdkContextApi,
) : State {

    override val name: String = "jsInitState"

    override fun prepare() {

    }

    override suspend fun active(): Result<Unit> {
        return runCatchingWithoutCancellation {
            sdkContext.config?.let {
                val jsEmarsysConfig = sdkContext.config as JsEmarsysConfig
                pushService.register(jsEmarsysConfig)
                pushService.subscribeForPushMessages(jsEmarsysConfig)
            }
        }
    }

    override fun relax() {
    }
}