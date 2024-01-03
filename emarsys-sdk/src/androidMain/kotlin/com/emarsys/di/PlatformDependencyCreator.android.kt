package com.emarsys.di

import android.content.Context
import com.emarsys.core.device.AndroidDeviceInfoCollector
import com.emarsys.core.device.AndroidLanguageProvider
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.storage.StorageApi
import com.emarsys.core.storage.Storage
import java.util.*

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) :
    DependencyCreator {
    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext


    private fun createLanguageProvider(): LanguageProvider {
        return AndroidLanguageProvider(Locale.getDefault())
    }

    private fun createAndroidDeviceInfoCollector(): AndroidDeviceInfoCollector {
        return AndroidDeviceInfoCollector(platformContext.application as Context, true)
    }

    override fun createDeviceInfoCollector(): DeviceInfoCollector {
        return DeviceInfoCollector(createAndroidDeviceInfoCollector(), createLanguageProvider())
    }

    override fun createStorage(): StorageApi = Storage(platformContext.sharedPreferences)

}