package com.emarsys.di

import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.storage.Storage

interface DependencyCreator {
    fun createStringStorage(): Storage
    fun createDeviceInfoCollector(): DeviceInfoCollector
}