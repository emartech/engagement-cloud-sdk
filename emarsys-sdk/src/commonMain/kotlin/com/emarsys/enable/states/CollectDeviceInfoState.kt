package com.emarsys.enable.states

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.context.RequestContext
import com.emarsys.core.state.State

internal class CollectDeviceInfoState(
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val requestContext: RequestContext,
) : State {
    override val name: String = "collectDeviceInfoState"
    override fun prepare() {}

    override suspend fun active() {
        val clientId = deviceInfoCollector.getClientId()
        requestContext.clientId = clientId
    }

    override fun relax() {}
}