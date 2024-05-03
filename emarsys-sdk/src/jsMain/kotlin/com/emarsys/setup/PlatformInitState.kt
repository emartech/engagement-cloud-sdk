package com.emarsys.setup

import com.emarsys.context.SdkContext
import com.emarsys.core.state.State
import com.emarsys.mobileengage.push.PushService

class PlatformInitState(private val pushService: PushService,
                        private val sdkContext: SdkContext): State {

    override val name: String = "jsInitState"

    override fun prepare() {

    }

    override suspend fun active() {
        sdkContext.config?.let {
            pushService.register(it)
        }
    }

    override fun relax() {
    }
}