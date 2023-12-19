package com.emarsys.di

import com.emarsys.core.storage.Storage
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.storage.StorageApi

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) : DependencyCreator {

    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext
    override fun createStorage(): StorageApi {
        return Storage(platformContext.storage)
    }

    override fun createDeviceInfoCollector(): DeviceInfoCollector {
        TODO("Not yet implemented")
    }
}