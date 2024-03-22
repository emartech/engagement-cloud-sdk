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
    private val applicationVersionProvider: Provider<String>
) : DeviceInfoCollectorApi {
    private companion object {
        const val HARDWARE_ID_STORAGE_KEY = "hardwareId"
    }

    actual override fun collect(): String {
        val headerData = webPlatformInfoCollector.collect()
        return Json.encodeToString(
            DeviceInfo(
                platform = headerData.browserName,
                applicationVersion = applicationVersionProvider.provide(),
                deviceModel = window.navigator.userAgent,
                osVersion = headerData.browserVersion,
                sdkVersion = BuildConfig.VERSION_NAME,
                language = window.navigator.language,
                timezone = timezoneProvider.provide()
            )
        )
    }

    override fun getHardwareId(): String {
        return storage.get(HARDWARE_ID_STORAGE_KEY) ?: run {
            val generatedId = uuidProvider.provide()
            storage.put(HARDWARE_ID_STORAGE_KEY, generatedId)
            generatedId
        }
    }
}