package com.sap.ec.core.device

internal interface DeviceInfoStorageApi {
    fun store(deviceInfo: String)

    fun clear()

    suspend fun hasDeviceInfoChanged(actualDeviceInfo: String): Boolean
}