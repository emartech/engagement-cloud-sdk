package com.sap.ec.enable.states

import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.state.State
import com.sap.ec.util.runCatchingWithoutCancellation

internal class CollectDeviceInfoState(
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val requestContext: RequestContextApi,
) : State {
    override val name: String = "collectDeviceInfoState"
    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        return runCatchingWithoutCancellation {
            val clientId = deviceInfoCollector.getClientId()
            requestContext.clientId = clientId
        }
    }

    override fun relax() {}
}