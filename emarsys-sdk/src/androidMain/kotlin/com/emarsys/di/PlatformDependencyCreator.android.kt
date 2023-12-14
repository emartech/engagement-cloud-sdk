package com.emarsys.di

import PlatformContext
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StringStorage

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) :
    DependencyCreator {
    private val platformContext: CommonPlatformContext = platformContext as CommonPlatformContext

    fun createDeviceInfoCollector(): DeviceInfoCollector {
        return DeviceInfoCollector(platformContext.application)
    }

    override fun createStringStorage(): Storage = StringStorage(platformContext.sharedPreferences)

}