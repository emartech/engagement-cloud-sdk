package com.sap.ec.core.device

import com.sap.ec.core.storage.StorageConstants.DEVICE_INFO_STORAGE_KEY
import com.sap.ec.core.storage.StringStorageApi

internal class DeviceInfoStorage(
    private val stringStorage: StringStorageApi
) : DeviceInfoStorageApi {

    override fun store(deviceInfo: String) {
        stringStorage.put(DEVICE_INFO_STORAGE_KEY, deviceInfo)
    }

    override fun clear() {
        stringStorage.put(DEVICE_INFO_STORAGE_KEY, null)
    }

    override suspend fun hasDeviceInfoChanged(actualDeviceInfo: String): Boolean {
        val storedDeviceInfo = stringStorage.get(DEVICE_INFO_STORAGE_KEY)
        return storedDeviceInfo != actualDeviceInfo
    }
}