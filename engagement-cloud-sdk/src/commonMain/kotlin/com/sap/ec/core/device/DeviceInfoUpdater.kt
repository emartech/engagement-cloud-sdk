package com.sap.ec.core.device

import com.sap.ec.core.device.DeviceConstants.DEVICE_INFO_STORAGE_KEY
import com.sap.ec.core.storage.StringStorageApi

class DeviceInfoUpdater(
    private val stringStorage: StringStorageApi
) : DeviceInfoUpdaterApi {

    override fun updateDeviceInfoHash(deviceInfo: String) {
        stringStorage.put(DEVICE_INFO_STORAGE_KEY, deviceInfo.hashCode().toString())
    }

    override suspend fun hasDeviceInfoChanged(actualDeviceInfo: String): Boolean {
        val storedHash = stringStorage.get(DEVICE_INFO_STORAGE_KEY)
        val actualHash = actualDeviceInfo.hashCode().toString()
        return storedHash != actualHash
    }
}