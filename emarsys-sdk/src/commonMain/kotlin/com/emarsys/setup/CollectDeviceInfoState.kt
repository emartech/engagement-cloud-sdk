package com.emarsys.setup

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.session.SessionContext

class CollectDeviceInfoState(
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val sessionContext: SessionContext
) : CollectDeviceInfoStateApi {
    override val name: String = "collectDeviceInfoState"
    override fun prepare() {}

    override suspend fun active() {
        val hardwareId = deviceInfoCollector.getHardwareId()
        sessionContext.clientId = hardwareId
    }

    override fun relax() {}
}