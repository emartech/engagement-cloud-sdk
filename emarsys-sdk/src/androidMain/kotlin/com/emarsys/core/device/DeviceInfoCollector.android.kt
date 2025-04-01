package com.emarsys.core.device

import android.content.pm.ApplicationInfo
import android.os.Build
import com.emarsys.SdkConstants
import com.emarsys.applicationContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.providers.TimezoneProviderApi
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.wrapper.WrapperInfo
import kotlinx.serialization.json.Json

internal actual class DeviceInfoCollector(
    private val timezoneProvider: TimezoneProviderApi,
    private val languageProvider: LanguageProviderApi,
    private val applicationVersionProvider: ApplicationVersionProviderApi,
    private val isGooglePlayServicesAvailable: Boolean,
    private val clientIdProvider: Provider<String>,
    private val platformInfoCollector: PlatformInfoCollectorApi,
    private val wrapperInfoStorage: TypedStorageApi,
    private val json: Json,
    private val stringStorage: StringStorageApi,
    private val sdkContext: SdkContextApi
) : DeviceInfoCollectorApi {

    actual override suspend fun collect(): String {
        return json.encodeToString(collectAsDeviceInfo())
    }

    actual override suspend fun collectAsDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            platform = getPlatform(),
            platformCategory = SdkConstants.MOBILE_PLATFORM_CATEGORY,
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
            platformCategory = SdkConstants.MOBILE_PLATFORM_CATEGORY,
            platformWrapper = getWrapperInfo()?.platformWrapper,
            platformWrapperVersion = getWrapperInfo()?.wrapperVersion,
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            isDebugMode = (0 != applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE),
            applicationCode = sdkContext.config?.applicationCode,
            merchantId = sdkContext.config?.merchantId,
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

    actual override suspend fun getPushSettings(): PushSettings {
        return platformInfoCollector.notificationSettings()
    }
}