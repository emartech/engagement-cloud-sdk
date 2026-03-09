package com.sap.ec.core.device

internal interface DeviceInfoUpdaterApi {
    fun storeDeviceInfo(deviceInfo: String)
    suspend fun hasDeviceInfoChanged(actualDeviceInfo: String): Boolean
}