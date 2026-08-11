package com.sap.ec.disable.states

import com.sap.ec.core.device.DeviceInfoStorageApi
import com.sap.ec.core.state.State

internal class ClearDeviceInfoState(private val deviceInfoUpdater: DeviceInfoStorageApi) : State {
    override val name: String
        get() = "clearDeviceInfo"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return try {
            deviceInfoUpdater.clear()
            Result.success(Unit)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    override fun relax() {
    }
}