package com.emarsys.setup

import com.emarsys.push.PushService

class PlatformInitState(private val pushService: PushService): PlatformInitStateApi {

    override val name: String = "jsInitState"

    override fun prepare() {
        TODO("Not yet implemented")
    }

    override suspend fun active() {
        pushService.register()
    }

    override fun relax() {
        TODO("Not yet implemented")
    }
}