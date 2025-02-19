package com.emarsys.setup

import com.emarsys.JsEmarsysConfig
import com.emarsys.context.SdkContext
import com.emarsys.core.state.State
import com.emarsys.mobileengage.push.PushServiceApi

class PlatformInitState(
    private val pushService: PushServiceApi,
    val sdkContext: SdkContext,
) : State {

    override val name: String = "jsInitState"

    override fun prepare() {

    }

    override suspend fun active() {
        sdkContext.config?.let {
            val jsEmarsysConfig = sdkContext.config as JsEmarsysConfig
            pushService.register(jsEmarsysConfig)
            pushService.subscribeForPushMessages(jsEmarsysConfig)
        }
    }

    override fun relax() {
    }
}