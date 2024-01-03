package com.emarsys.core.device

expect class DeviceInfoCollector: DeviceInfoCollectorApi {
    override fun collect(): String
}