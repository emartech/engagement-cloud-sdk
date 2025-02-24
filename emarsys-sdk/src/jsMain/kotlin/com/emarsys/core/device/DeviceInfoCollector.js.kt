package com.emarsys.core.device

import com.emarsys.core.providers.Provider
import kotlinx.browser.window
import kotlinx.serialization.json.Json

actual class DeviceInfoCollector(
    private val clientIdProvider: Provider<String>,
    private val timezoneProvider: Provider<String>,
    private val webPlatformInfoCollector: WebPlatformInfoCollectorApi,
    private val applicationVersionProvider: Provider<String>,
    private val languageProvider: Provider<String>,
    private val json: Json
) : DeviceInfoCollectorApi {

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
                timezone = timezoneProvider.provide(),
                clientId = getClientId()
            )
        )
    }

    actual override fun getClientId(): String {
        return clientIdProvider.provide()
    }

    actual override suspend fun getPushSettings(): PushSettings {
        TODO("Not yet implemented")
    }
}