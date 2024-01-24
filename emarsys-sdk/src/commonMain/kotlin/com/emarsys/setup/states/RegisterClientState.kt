package com.emarsys.setup.states

import com.emarsys.core.state.State
import com.emarsys.networking.clients.device.DeviceClientApi

class RegisterClientState(
    private val deviceClient: DeviceClientApi
) : State {
    override val name: String
        get() = "registerClientState"

    override fun prepare() {
    }

    override suspend fun active() {
        deviceClient.registerDeviceInfo()
    }

    override fun relax() {
    }
}