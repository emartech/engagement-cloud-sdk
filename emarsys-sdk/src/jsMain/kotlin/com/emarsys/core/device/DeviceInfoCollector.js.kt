package com.emarsys.core.device

import com.emarsys.SdkConstants
import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.wrapper.WrapperInfo
import kotlinx.browser.window
import kotlinx.serialization.json.Json

actual class DeviceInfoCollector(
    private val clientIdProvider: Provider<String>,
    private val timezoneProvider: Provider<String>,
    private val webPlatformInfoCollector: WebPlatformInfoCollectorApi,
    private val applicationVersionProvider: Provider<String>,
    private val languageProvider: Provider<String>,
    private val wrapperInfoStorage: TypedStorageApi<WrapperInfo?>,
    private val json: Json
) : DeviceInfoCollectorApi {

    actual override suspend fun collect(): String {
        val headerData = webPlatformInfoCollector.collect()
        return json.encodeToString(
            DeviceInfo(
                platform = headerData.browserName,
                platformCategory = SdkConstants.WEB_PLATFORM_CATEGORY,
                platformWrapper = getWrapperInfo()?.platformWrapper,
                platformWrapperVersion = getWrapperInfo()?.wrapperVersion,
                applicationVersion = applicationVersionProvider.provide(),
                deviceModel = window.navigator.userAgent,
                osVersion = headerData.browserVersion,
                sdkVersion = BuildConfig.VERSION_NAME,
                language = languageProvider.provide(),
                timezone = timezoneProvider.provide(),
                clientId = getClientId()
            )
        )
    }

    private suspend fun getWrapperInfo(): WrapperInfo? {
        return wrapperInfoStorage.get(StorageConstants.WRAPPER_INFO_KEY)
    }

    actual override suspend fun getClientId(): String {
        return clientIdProvider.provide()
    }

    actual override suspend fun getPushSettings(): PushSettings {
        TODO("Not yet implemented")
    }
}