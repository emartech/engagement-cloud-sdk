package com.emarsys.core.device

import kotlinx.browser.window
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class DeviceInfoCollector(private val webDeviceInfoCollector: DeviceInfoCollectorApi) :
    DeviceInfoCollectorApi {
    actual override fun collect(): String {
        val platformInfo = webDeviceInfoCollector.collect()
        return Json.encodeToString(
            DeviceInformation(
                platform = window.navigator.platform,
                manufacturer = window.navigator.vendor,
                displayMetrics = "${window.innerWidth}x${window.innerHeight}",
                model = window.navigator.product,
                sdkVersion = BuildConfig.VERSION_NAME,
                language = window.navigator.language,
                timezone = Clock.System.now().offsetIn(TimeZone.currentSystemDefault()).toString(),
                hardwareId = "test hwid",
                platformInfo = platformInfo
            )
        )
    }
}