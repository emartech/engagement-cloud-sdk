package com.emarsys.core.device

import com.emarsys.core.providers.HardwareIdProvider

actual class DeviceInfoCollector(private val hardwareIdProvider: HardwareIdProvider) :
    DeviceInfoCollectorApi {
    actual override fun collect(): String {
        TODO("Not yet implemented")
    }

    actual override fun getHardwareId(): String {
        return hardwareIdProvider.provide()
    }

    actual override fun getPushSettings(): PushSettings {
        TODO("Not yet implemented")
    }

}