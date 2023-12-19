package com.emarsys.di

import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.storage.StorageApi

interface DependencyCreator {
    fun createStorage(): StorageApi
    fun createDeviceInfoCollector(): DeviceInfoCollector
}