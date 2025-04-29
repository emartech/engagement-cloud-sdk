package com.emarsys.core.device

import com.emarsys.SdkConstants
import com.emarsys.context.SdkContextApi
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.providers.TimezoneProviderApi
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.wrapper.WrapperInfo
import kotlinx.browser.window
import kotlinx.serialization.json.Json

internal actual class DeviceInfoCollector(
    private val clientIdProvider: Provider<String>,
    private val timezoneProvider: TimezoneProviderApi,
    private val webPlatformInfoCollector: WebPlatformInfoCollectorApi,
    private val applicationVersionProvider: ApplicationVersionProviderApi,
    private val languageProvider: LanguageProviderApi,
    private val wrapperInfoStorage: TypedStorageApi,
    private val json: Json,
    private val stringStorage: StringStorageApi,
    private val sdkContext: SdkContextApi
) : DeviceInfoCollectorApi {

    actual override suspend fun collect(): String {
        return json.encodeToString(collectAsDeviceInfo())
    }

    actual override suspend fun collectAsDeviceInfo(): DeviceInfo {
        val headerData = webPlatformInfoCollector.collect()

        return DeviceInfo(
            platform = headerData.browserName,
            platformCategory = SdkConstants.WEB_PLATFORM_CATEGORY,
            platformWrapper = getWrapperInfo()?.platformWrapper,
            platformWrapperVersion = getWrapperInfo()?.wrapperVersion,
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = window.navigator.userAgent,
            osVersion = headerData.browserVersion,
            sdkVersion = BuildConfig.VERSION_NAME,
            language = stringStorage.get(SdkConstants.LANGUAGE_STORAGE_KEY)
                ?: languageProvider.provide(),
            timezone = timezoneProvider.provide(),
            clientId = getClientId()
        )
    }

    actual override suspend fun collectAsDeviceInfoForLogs(): DeviceInfoForLogs {
        val deviceInfo = collectAsDeviceInfo()
        return DeviceInfoForLogs(
            platform = deviceInfo.platform,
            platformCategory = deviceInfo.platformCategory,
            platformWrapper = deviceInfo.platformWrapper,
            platformWrapperVersion = deviceInfo.platformWrapperVersion,
            applicationVersion = deviceInfo.applicationVersion,
            deviceModel = deviceInfo.deviceModel,
            osVersion = deviceInfo.osVersion,
            sdkVersion = deviceInfo.sdkVersion,
            isDebugMode = false,
            applicationCode = sdkContext.config?.applicationCode,
            merchantId = sdkContext.config?.merchantId,
            language = deviceInfo.language,
            timezone = deviceInfo.timezone,
            clientId = deviceInfo.clientId
        )
    }

    private suspend fun getWrapperInfo(): WrapperInfo? {
        return wrapperInfoStorage.get(StorageConstants.WRAPPER_INFO_KEY, WrapperInfo.serializer())
    }

    actual override suspend fun getClientId(): String {
        return clientIdProvider.provide()
    }

    actual override suspend fun getNotificationSettings(): NotificationSettings {
        TODO("Not yet implemented")
    }
}