package com.emarsys.core.device

import com.emarsys.KotlinPlatform
import com.emarsys.core.providers.Provider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class DeviceInfoCollector(
    private val hardwareIdProvider: Provider<String>,
    private val applicationVersionProvider: Provider<String>,
    private val languageProvider: Provider<String>,
    private val timezoneProvider: Provider<String>,
    private val deviceInformation: UIDeviceApi,
    private val json: Json
) : DeviceInfoCollectorApi {
    actual override fun collect(): String {
        val deviceInfo = DeviceInfo(
            KotlinPlatform.IOS.name,
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = deviceInformation.deviceModel(),
            osVersion = deviceInformation.osVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            languageCode = languageProvider.provide(),
            timezone = timezoneProvider.provide()
        )
        return json.encodeToString(deviceInfo)
    }

    actual override fun getHardwareId(): String {
        return hardwareIdProvider.provide()
    }

    actual override fun getPushSettings(): PushSettings {
        TODO("Not yet implemented")
    }

}