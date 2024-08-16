package com.emarsys.core.device

import android.os.Build
import com.emarsys.core.providers.Provider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class DeviceInfoCollector(
    private val timezoneProvider: Provider<String>,
    private val languageProvider: Provider<String>,
    private val applicationVersionProvider: Provider<String>,
    private val isGooglePlayServicesAvailable: Boolean,
    private val hardwareIdProvider: Provider<String>,
    private val platformInfoCollector: PlatformInfoCollectorApi,
    private val json: Json
) : DeviceInfoCollectorApi {

    actual override fun collect(): String {
        val deviceInfo = DeviceInfo(
            platform = getPlatform(),
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            languageCode = languageProvider.provide(),
            timezone = timezoneProvider.provide()
        )

        return json.encodeToString(deviceInfo)
    }

    actual override fun getHardwareId(): String {
        return hardwareIdProvider.provide()
    }

    private fun getPlatform(): String {
        return if (isGooglePlayServicesAvailable) "android" else "android-huawei"
    }

    actual override suspend fun getPushSettings(): PushSettings {
        return platformInfoCollector.notificationSettings()
    }
}