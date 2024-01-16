package com.emarsys.di

import com.emarsys.api.push.PushApi
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.WebPlatformInfoCollector
import com.emarsys.core.storage.StorageApi
import com.emarsys.core.storage.StringStorage
import com.emarsys.providers.Provider
import com.emarsys.setup.PlatformInitState
import com.emarsys.setup.PlatformInitStateApi
import kotlinx.browser.window

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) : DependencyCreator {

    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext

    override fun createStorage(): StorageApi<String> {
        return StringStorage(platformContext.storage)
    }

    override fun createDeviceInfoCollector(uuidProvider: Provider<String>): DeviceInfoCollector {
        return DeviceInfoCollector(createWebDeviceInfoCollector(), uuidProvider, createStorage())
    }

    override fun createPlatformInitState(pushApi: PushApi): PlatformInitStateApi {
        return PlatformInitState(pushApi)
    }

    private fun createWebDeviceInfoCollector(): WebPlatformInfoCollector {
        return WebPlatformInfoCollector(getNavigatorData())
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