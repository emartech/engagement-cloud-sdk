package com.sap.ec.core.device

import com.sap.ec.SdkConstants
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.device.notification.PermissionState
import com.sap.ec.core.device.notification.WebNotificationSettingsCollectorApi
import com.sap.ec.core.providers.ApplicationVersionProviderApi
import com.sap.ec.core.providers.LanguageProviderApi
import com.sap.ec.core.providers.Provider
import com.sap.ec.core.providers.TimezoneProviderApi
import com.sap.ec.core.providers.platform.PlatformCategoryProviderApi
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.core.storage.TypedStorageApi
import com.sap.ec.core.wrapper.WrapperInfo
import kotlinx.browser.window
import kotlinx.serialization.json.Json

internal actual class DeviceInfoCollector(
    private val clientIdProvider: Provider<String>,
    private val timezoneProvider: TimezoneProviderApi,
    private val webPlatformInfoCollector: WebPlatformInfoCollectorApi,
    private val applicationVersionProvider: ApplicationVersionProviderApi,
    private val languageProvider: LanguageProviderApi,
    private val wrapperInfoStorage: TypedStorageApi,
    private val webNotificationSettingsCollector: WebNotificationSettingsCollectorApi,
    private val json: Json,
    private val stringStorage: StringStorageApi,
    private val sdkContext: SdkContextApi,
    private val platformCategoryProvider: PlatformCategoryProviderApi
) : DeviceInfoCollectorApi {

    actual override suspend fun collect(): String {
        return json.encodeToString(collectAsDeviceInfo())
    }

    actual override suspend fun collectAsDeviceInfo(): DeviceInfo {
        val headerData = webPlatformInfoCollector.collect()

        return DeviceInfo(
            platform = headerData.browserName,
            platformCategory = platformCategoryProvider.provide(),
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
        return NotificationSettings(
            webNotificationSettingsCollector.collect().permissionState == PermissionState.Granted
        )
    }

    actual override fun getPlatformCategory(): String {
        return platformCategoryProvider.provide()
    }
}