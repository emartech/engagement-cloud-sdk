package com.sap.ec.core.device

interface DeviceInfoUpdaterApi {
    fun updateDeviceInfoHash(deviceInfo: String)
    suspend fun hasDeviceInfoChanged(actualDeviceInfo: String): Boolean
}