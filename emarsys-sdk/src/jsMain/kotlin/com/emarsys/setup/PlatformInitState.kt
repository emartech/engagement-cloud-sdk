package com.emarsys.setup

import com.emarsys.context.SdkContextApi
import com.emarsys.core.state.State
import com.emarsys.mobileengage.inapp.InappJsBridgeApi
import com.emarsys.mobileengage.push.PushServiceApi

class PlatformInitState(
    private val pushService: PushServiceApi,
    private val sdkContext: SdkContextApi,
    private val inappJsBridge: InappJsBridgeApi
): State {

    override val name: String = "jsInitState"

    override fun prepare() {

    }

    override suspend fun active() {
        sdkContext.config?.let {
            pushService.register(it)
            inappJsBridge.register()
        }
    }

    override fun relax() {
    }
}