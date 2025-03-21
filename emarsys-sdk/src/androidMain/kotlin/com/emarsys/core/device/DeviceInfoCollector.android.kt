package com.emarsys.core.device

import android.os.Build
import com.emarsys.SdkConstants
import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.wrapper.WrapperInfo
import kotlinx.serialization.json.Json

actual class DeviceInfoCollector(
    private val timezoneProvider: Provider<String>,
    private val languageProvider: Provider<String>,
    private val applicationVersionProvider: Provider<String>,
    private val isGooglePlayServicesAvailable: Boolean,
    private val clientIdProvider: Provider<String>,
    private val platformInfoCollector: PlatformInfoCollectorApi,
    private val wrapperInfoStorage: TypedStorageApi,
    private val json: Json
    private val wrapperInfoStorage: TypedStorageApi<WrapperInfo?>,
    private val json: Json,
    private val stringStorage: StringStorageApi
) : DeviceInfoCollectorApi {

    actual override suspend fun collect(): String {
        val deviceInfo = DeviceInfo(
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

        return json.encodeToString(deviceInfo)
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