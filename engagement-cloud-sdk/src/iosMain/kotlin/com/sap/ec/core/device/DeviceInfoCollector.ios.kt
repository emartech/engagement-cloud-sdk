package com.sap.ec.core.device

import com.sap.ec.KotlinPlatform
import com.sap.ec.SdkConstants
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.device.notification.IosNotificationSettingsCollectorApi
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
import kotlin.experimental.ExperimentalNativeApi

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class DeviceInfoCollector(
    private val clientIdProvider: Provider<String>,
    private val applicationVersionProvider: ApplicationVersionProviderApi,
    private val languageProvider: LanguageProviderApi,
    private val timezoneProvider: TimezoneProviderApi,
    private val deviceInformation: UIDeviceApi,
    private val wrapperInfoStorage: TypedStorageApi,
    private val iosNotificationSettingsCollector: IosNotificationSettingsCollectorApi,
    private val json: Json,
    private val stringStorage: StringStorageApi,
    private val sdkContext: SdkContextApi,
    private val platformCategoryProvider: PlatformCategoryProviderApi,
) : DeviceInfoCollectorApi {
    actual override suspend fun collect(): String {
        return json.encodeToString(collectAsDeviceInfo())
    }

    actual override suspend fun collectAsDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            KotlinPlatform.IOS.name.lowercase(),
            platformCategory = platformCategoryProvider.provide(),
            platformWrapper = getWrapperInfo()?.platformWrapper,
            platformWrapperVersion = getWrapperInfo()?.wrapperVersion,
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = deviceInformation.deviceModel(),
            osVersion = deviceInformation.osVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            language = stringStorage.get(SdkConstants.LANGUAGE_STORAGE_KEY)
                ?: languageProvider.provide(),
            timezone = timezoneProvider.provide(),
            clientId = clientIdProvider.provide()
        )
    }

    @OptIn(ExperimentalNativeApi::class)
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
            isDebugMode = Platform.isDebugBinary,
            applicationCode = sdkContext.config?.applicationCode,
            language = deviceInfo.language,
            timezone = deviceInfo.timezone,
            clientId = clientIdProvider.provide()
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
            iosNotificationSettingsCollector.collect().authorizationStatus == IosAuthorizationStatus.Authorized
        )
    }

    actual override fun getPlatformCategory(): String {
        return platformCategoryProvider.provide()
    }
}
