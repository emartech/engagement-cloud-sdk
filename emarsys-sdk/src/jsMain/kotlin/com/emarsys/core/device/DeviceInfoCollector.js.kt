package com.emarsys.core.device

import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.providers.Provider
import kotlinx.browser.window
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class DeviceInfoCollector(
    private val webPlatformInfoCollector: PlatformInfoCollectorApi,
    private val uuidProvider: Provider<String>,
    private val storage: TypedStorageApi<String?>
) : DeviceInfoCollectorApi {
    private companion object {
        const val HARDWARE_ID_STORAGE_KEY = "hardwareId"
    }

    actual override fun collect(): String {
        val platformInfo = webPlatformInfoCollector.collect()
        return Json.encodeToString(
            DeviceInformation(
                platform = window.navigator.platform,
                manufacturer = window.navigator.vendor,
                displayMetrics = "${window.innerWidth}x${window.innerHeight}",
                model = window.navigator.product,
                sdkVersion = BuildConfig.VERSION_NAME,
                language = window.navigator.language,
                timezone = Clock.System.now().offsetIn(TimeZone.currentSystemDefault()).toString(),
                hardwareId = getHardwareId(),
                platformInfo = platformInfo,
                applicationVersion = webPlatformInfoCollector.applicationVersion()
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