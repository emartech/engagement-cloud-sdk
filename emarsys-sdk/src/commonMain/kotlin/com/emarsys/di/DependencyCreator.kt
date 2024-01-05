package com.emarsys.di

import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.storage.StorageApi
import com.emarsys.providers.Provider

interface DependencyCreator {
    fun createStorage():  StorageApi<String>

    fun createDeviceInfoCollector(uuidProvider: Provider<String>): DeviceInfoCollector

}