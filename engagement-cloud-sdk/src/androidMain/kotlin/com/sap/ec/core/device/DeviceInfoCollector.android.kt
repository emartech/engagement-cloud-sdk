package com.sap.ec.core.device

import android.content.pm.ApplicationInfo
import android.os.Build
import com.sap.ec.SdkConstants
import com.sap.ec.applicationContext
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.device.notification.AndroidNotificationSettingsCollectorApi
import com.sap.ec.core.providers.ApplicationVersionProviderApi
import com.sap.ec.core.providers.LanguageProviderApi
import com.sap.ec.core.providers.Provider
import com.sap.ec.core.providers.TimezoneProviderApi
import com.sap.ec.core.providers.platform.PlatformCategoryProviderApi
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.core.storage.TypedStorageApi
import com.sap.ec.core.wrapper.WrapperInfo
import kotlinx.serialization.json.Json

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class DeviceInfoCollector(
    private val timezoneProvider: TimezoneProviderApi,
    private val languageProvider: LanguageProviderApi,
    private val applicationVersionProvider: ApplicationVersionProviderApi,
    private val isGooglePlayServicesAvailable: Boolean,
    private val clientIdProvider: Provider<String>,
    private val platformInfoCollector: PlatformInfoCollectorApi,
    private val wrapperInfoStorage: TypedStorageApi,
    private val androidNotificationSettingsCollector: AndroidNotificationSettingsCollectorApi,
    private val json: Json,
    private val stringStorage: StringStorageApi,
    private val sdkContext: SdkContextApi,
    private val platformCategoryProvider: PlatformCategoryProviderApi
) : DeviceInfoCollectorApi {

    actual override suspend fun collect(): String {
        return json.encodeToString(collectAsDeviceInfo())
    }

    actual override suspend fun collectAsDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            platform = getPlatform(),
            platformCategory = platformCategoryProvider.provide(),
            platformWrapper = getWrapperInfo()?.platformWrapper,
            platformWrapperVersion = getWrapperInfo()?.wrapperVersion,
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            language = language(),
            timezone = timezoneProvider.provide(),
            clientId = clientIdProvider.provide()
        )
    }

    actual override suspend fun collectAsDeviceInfoForLogs(): DeviceInfoForLogs {
        return DeviceInfoForLogs(
            platform = getPlatform(),
            platformCategory = platformCategoryProvider.provide(),
            platformWrapper = getWrapperInfo()?.platformWrapper,
            platformWrapperVersion = getWrapperInfo()?.wrapperVersion,
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            isDebugMode = (0 != applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE),
            applicationCode = sdkContext.config?.applicationCode,
            language = language(),
            timezone = timezoneProvider.provide(),
            clientId = clientIdProvider.provide()
        )
    }

    private suspend fun language(): String {
        return stringStorage.get(SdkConstants.LANGUAGE_STORAGE_KEY) ?: languageProvider.provide()
    }

    private suspend fun getWrapperInfo(): WrapperInfo? {
        return wrapperInfoStorage.get(StorageConstants.WRAPPER_INFO_KEY, WrapperInfo.serializer())
    }

    actual override suspend fun getClientId(): String {
        return clientIdProvider.provide()
    }

    private fun getPlatform(): String {
        return if (isGooglePlayServicesAvailable) "android" else "android-huawei"
    }

    actual override suspend fun getNotificationSettings(): NotificationSettings {
        return NotificationSettings(androidNotificationSettingsCollector.collect().areNotificationsEnabled)
    }

    actual override fun getPlatformCategory(): String {
        return platformCategoryProvider.provide()
    }
}