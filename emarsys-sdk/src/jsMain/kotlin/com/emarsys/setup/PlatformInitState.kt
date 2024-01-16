package com.emarsys.setup

import com.emarsys.api.push.PushApi
import com.emarsys.core.state.State

class PlatformInitState(pushApi: PushApi): PlatformInitStateApi {

    override val name: String = "jsInitState"

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