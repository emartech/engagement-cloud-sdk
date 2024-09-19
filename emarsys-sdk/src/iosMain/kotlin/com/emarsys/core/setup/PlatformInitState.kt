package com.emarsys.core.setup

import com.emarsys.core.state.State
import com.emarsys.mobileengage.push.IosPushInstance

class PlatformInitState(private val iosPushInternal: IosPushInstance): State {
    override val name: String = "iOSInitState"
    override fun prepare() {
    }

    override suspend fun active() {
        iosPushInternal.registerEmarsysNotificationCenterDelegate()
    }

    override fun relax() {
    }
}