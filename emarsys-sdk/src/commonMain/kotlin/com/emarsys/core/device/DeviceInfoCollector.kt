package com.emarsys.core.device

expect class DeviceInfoCollector {
    fun collectDeviceInfoRequest(): String
    fun deviceType(): String
    fun osVersion(): String
    fun hardwareId(): String

}