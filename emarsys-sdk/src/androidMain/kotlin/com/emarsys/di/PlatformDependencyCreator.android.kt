package com.emarsys.di

import android.content.Context
import com.emarsys.core.device.AndroidPlatformInfoCollector
import com.emarsys.core.device.AndroidLanguageProvider
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.storage.StorageApi
import com.emarsys.core.storage.StringStorage
import com.emarsys.providers.Provider
import java.util.*

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) :
    DependencyCreator {
    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext


    private fun createLanguageProvider(): LanguageProvider {
        return AndroidLanguageProvider(Locale.getDefault())
    }

    private fun createAndroidDeviceInfoCollector(): AndroidPlatformInfoCollector {
        return AndroidPlatformInfoCollector(platformContext.application as Context)
    }

    override fun createDeviceInfoCollector(uuidProvider: Provider<String>): DeviceInfoCollector {
        return DeviceInfoCollector(
            createAndroidDeviceInfoCollector(),
            createLanguageProvider(),
            uuidProvider,
            createStorage(),
            true,
        )
    }

    override fun createStorage(): StorageApi<String> = StringStorage(platformContext.sharedPreferences)

}