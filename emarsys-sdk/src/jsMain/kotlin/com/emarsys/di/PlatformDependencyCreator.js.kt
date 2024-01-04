package com.emarsys.di

import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.WebDeviceInfoCollector
import com.emarsys.core.storage.StorageApi
import com.emarsys.core.storage.StringStorage
import kotlinx.browser.window

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) : DependencyCreator {

    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext
    override fun createStorage(): StorageApi<String> {
        return StringStorage(platformContext.storage)
    }

    override fun createDeviceInfoCollector(): DeviceInfoCollector {
        return DeviceInfoCollector(createWebDeviceInfoCollector())
    }

    private fun createWebDeviceInfoCollector(): WebDeviceInfoCollector {
        return WebDeviceInfoCollector(getNavigatorData())
    }

    private fun getNavigatorData(): String {
        return listOf(
            window.navigator.platform,
            window.navigator.userAgent,
            window.navigator.appVersion,
            window.navigator.vendor,
        ).joinToString(" ")
    }
}