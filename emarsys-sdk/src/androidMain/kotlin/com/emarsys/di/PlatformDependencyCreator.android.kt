package com.emarsys.di

import PlatformContext
import com.emarsys.core.device.AndroidLanguageProvider
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StringStorage
import java.util.*

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) :
    DependencyCreator {
    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext


    private fun createLanguageProvider(): LanguageProvider {
        return AndroidLanguageProvider(Locale.getDefault())
    }

    override fun createDeviceInfoCollector(): DeviceInfoCollector {
        return DeviceInfoCollector(platformContext.application, createLanguageProvider(), true, true)
    }

    override fun createStringStorage(): Storage = StringStorage(platformContext.sharedPreferences)

}