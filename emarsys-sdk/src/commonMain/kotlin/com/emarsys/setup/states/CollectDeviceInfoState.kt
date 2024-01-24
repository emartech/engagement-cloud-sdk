package com.emarsys.setup.states

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.state.State
import com.emarsys.session.SessionContext

class CollectDeviceInfoState(
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val sessionContext: SessionContext,
) : State {
    override val name: String = "collectDeviceInfoState"
    override fun prepare() {}

    override suspend fun active() {
        val hardwareId = deviceInfoCollector.getHardwareId()
        sessionContext.clientId = hardwareId
    }

    override fun relax() {}
}