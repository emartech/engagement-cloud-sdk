package com.emarsys.core.device.fakes

import com.emarsys.core.device.DeviceInfoCollectorApi

class FakeWebDeviceInfoCollector(private val onCollectCalled: () -> String): DeviceInfoCollectorApi {

    override fun collect(): String {
        return onCollectCalled()
    }
}