package com.emarsys.core.device

import android.os.Build
import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.TypedStorageApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class DeviceInfoCollector(
    private val uuidProvider: Provider<String>,
    private val timezoneProvider: Provider<String>,
    private val languageProvider: LanguageProvider,
    private val applicationVersionProvider: Provider<String>,
    private val storage: TypedStorageApi<String?>,
    private val isGooglePlayServicesAvailable: Boolean,
) : DeviceInfoCollectorApi {
    private companion object {
        const val HARDWARE_ID_STORAGE_KEY = "hardwareId"
    }

    actual override fun collect(): String {
        val deviceInfo = DeviceInfo(
            platform = getPlatform(),
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            language = languageProvider.provideLanguage(),
            timezone = timezoneProvider.provide()
        )

        return Json.encodeToString(deviceInfo)
    }

    override fun getHardwareId(): String {
        return storage.get(HARDWARE_ID_STORAGE_KEY) ?: run {
            val generatedId = uuidProvider.provide()
            storage.put(HARDWARE_ID_STORAGE_KEY, generatedId)
            generatedId
        }
    }

    private fun getPlatform(): String {
        return if (isGooglePlayServicesAvailable) "android" else "android-huawei"
    }
}