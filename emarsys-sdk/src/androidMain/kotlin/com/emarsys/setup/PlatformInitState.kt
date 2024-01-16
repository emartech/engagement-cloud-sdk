package com.emarsys.setup

import com.emarsys.api.push.PushApi

class PlatformInitState(val pushApi: PushApi): PlatformInitStateApi {

    override val name: String = "androidInitState"

    override fun prepare() {
        TODO("Not yet implemented")
    }

    override suspend fun active() {
        TODO("Not yet implemented")
    }

    override fun relax() {
        TODO("Not yet implemented")
    }
}