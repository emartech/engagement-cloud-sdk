package com.emarsys.core.device

import android.os.Build
import com.emarsys.core.providers.Provider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class DeviceInfoCollector(
    private val timezoneProvider: Provider<String>,
    private val languageProvider: LanguageProvider,
    private val applicationVersionProvider: Provider<String>,
    private val isGooglePlayServicesAvailable: Boolean,
    private val hardwareIdProvider: Provider<String>
) : DeviceInfoCollectorApi {

    actual override fun collect(): String {
        val deviceInfo = DeviceInfo(
            platform = getPlatform(),
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            languageCode = languageProvider.provideLanguage(),
            timezone = timezoneProvider.provide()
        )

        return Json.encodeToString(deviceInfo)
    }

    actual override fun getHardwareId(): String {
        return hardwareIdProvider.provide()
    }

    private fun getPlatform(): String {
        return if (isGooglePlayServicesAvailable) "android" else "android-huawei"
    }

    actual override fun getPushSettings(): PushSettings {
        TODO("Not yet implemented")
    }
}