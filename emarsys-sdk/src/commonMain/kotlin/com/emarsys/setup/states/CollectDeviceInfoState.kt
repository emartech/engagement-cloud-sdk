package com.emarsys.setup.states

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.session.SessionContext
import com.emarsys.core.state.State

class CollectDeviceInfoState(
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val sessionContext: SessionContext,
) : State {
    override val name: String = "collectDeviceInfoState"
    override fun prepare() {}

    override suspend fun active() {
        val clientId = deviceInfoCollector.getClientId()
        sessionContext.clientId = clientId
    }

    override fun relax() {}
}