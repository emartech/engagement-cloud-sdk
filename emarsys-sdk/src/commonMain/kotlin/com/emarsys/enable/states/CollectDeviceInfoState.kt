package com.emarsys.enable.states

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.state.State
import com.emarsys.util.runCatchingWithoutCancellation

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