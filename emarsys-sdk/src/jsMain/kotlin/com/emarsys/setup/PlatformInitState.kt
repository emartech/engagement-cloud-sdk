package com.emarsys.setup

import com.emarsys.core.state.State
import com.emarsys.push.PushService

class PlatformInitState(private val pushService: PushService): State {

    override val name: String = "jsInitState"

    override fun prepare() {

    }

    override suspend fun active() {
        pushService.register()
    }

    override fun relax() {
    }
}