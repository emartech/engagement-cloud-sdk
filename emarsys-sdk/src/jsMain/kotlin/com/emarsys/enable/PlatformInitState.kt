package com.emarsys.enable

import com.emarsys.JsEmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.State
import com.emarsys.mobileengage.push.PushServiceApi

internal class PlatformInitState(
    private val pushService: PushServiceApi,
    val sdkContext: SdkContextApi,
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