package com.sap.ec.core.device

import com.sap.ec.core.device.DeviceConstants.DEVICE_INFO_STORAGE_KEY
import com.sap.ec.core.storage.StringStorageApi

class DeviceInfoUpdater(
    private val stringStorage: StringStorageApi
) : DeviceInfoUpdaterApi {

    override fun storeDeviceInfo(deviceInfo: String) {
        stringStorage.put(DEVICE_INFO_STORAGE_KEY, deviceInfo)
    }

    override suspend fun hasDeviceInfoChanged(actualDeviceInfo: String): Boolean {
        val storedDeviceInfo = stringStorage.get(DEVICE_INFO_STORAGE_KEY)
        return storedDeviceInfo != actualDeviceInfo
    }
}