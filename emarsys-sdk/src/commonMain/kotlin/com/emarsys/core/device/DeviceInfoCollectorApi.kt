package com.emarsys.core.device

interface DeviceInfoCollectorApi {
    fun collect(): String

    fun getHardwareId(): String
}