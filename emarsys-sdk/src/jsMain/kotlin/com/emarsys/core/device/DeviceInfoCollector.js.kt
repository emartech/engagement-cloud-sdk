package com.emarsys.core.device

import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.TypedStorageApi
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class DeviceInfoCollector(
    private val uuidProvider: Provider<String>,
    private val timezoneProvider: Provider<String>,
    private val webPlatformInfoCollector: WebPlatformInfoCollectorApi,
    private val storage: TypedStorageApi<String?>,
    private val applicationVersionProvider: Provider<String>,
    private val languageProvider: Provider<String>,
    private val json: Json
) : DeviceInfoCollectorApi {
    private companion object {
        const val HARDWARE_ID_STORAGE_KEY = "hardwareId"
    }

    actual override fun collect(): String {
        val headerData = webPlatformInfoCollector.collect()
        return json.encodeToString(
            DeviceInfo(
                platform = headerData.browserName,
                applicationVersion = applicationVersionProvider.provide(),
                deviceModel = window.navigator.userAgent,
                osVersion = headerData.browserVersion,
                sdkVersion = BuildConfig.VERSION_NAME,
                languageCode = languageProvider.provide(),
                timezone = timezoneProvider.provide()
            )
        )
    }

    actual override fun getHardwareId(): String {
        return storage.get(HARDWARE_ID_STORAGE_KEY) ?: run {
            val generatedId = uuidProvider.provide()
            storage.put(HARDWARE_ID_STORAGE_KEY, generatedId)
            generatedId
        }
    }

    actual override fun getPushSettings(): PushSettings {
        TODO("Not yet implemented")
    }
}