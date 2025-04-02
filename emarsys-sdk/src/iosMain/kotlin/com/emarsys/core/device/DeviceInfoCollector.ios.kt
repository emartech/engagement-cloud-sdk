package com.emarsys.core.device

import com.emarsys.KotlinPlatform
import com.emarsys.SdkConstants
import com.emarsys.context.SdkContextApi
import com.emarsys.core.device.IosNotificationConstant.Companion.fromLong
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.providers.TimezoneProviderApi
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.wrapper.WrapperInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.experimental.ExperimentalNativeApi

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class DeviceInfoCollector(
    private val clientIdProvider: Provider<String>,
    private val applicationVersionProvider: ApplicationVersionProviderApi,
    private val languageProvider: LanguageProviderApi,
    private val timezoneProvider: TimezoneProviderApi,
    private val deviceInformation: UIDeviceApi,
    private val wrapperInfoStorage: TypedStorageApi,
    private val json: Json,
    private val stringStorage: StringStorageApi,
    private val sdkContext: SdkContextApi,
) : DeviceInfoCollectorApi {
    actual override suspend fun collect(): String {
        return json.encodeToString(collectAsDeviceInfo())
    }

    actual override suspend fun collectAsDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            KotlinPlatform.IOS.name.lowercase(),
            platformCategory = SdkConstants.MOBILE_PLATFORM_CATEGORY,
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
            merchantId = sdkContext.config?.merchantId,
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

    actual override suspend fun getPushSettings(): PushSettings {
        return suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter()
                .getNotificationSettingsWithCompletionHandler { settings ->
                    if (settings != null) {
                        val iosNotificationSettings = IosNotificationSettings(
                            fromLong<IosAuthorizationStatus>(settings.authorizationStatus),
                            fromLong<IosNotificationSetting>(settings.soundSetting),
                            fromLong<IosNotificationSetting>(settings.badgeSetting),
                            fromLong<IosNotificationSetting>(settings.alertSetting),
                            fromLong<IosNotificationSetting>(settings.notificationCenterSetting),
                            fromLong<IosNotificationSetting>(settings.lockScreenSetting),
                            fromLong<IosNotificationSetting>(settings.carPlaySetting),
                            fromLong<IosAlertStyle>(settings.alertStyle),
                            settings.showPreviewsSetting.name.toShowPreviewSetting(),
                            fromLong<IosNotificationSetting>(settings.criticalAlertSetting),
                            settings.providesAppNotificationSettings,
                            fromLong<IosNotificationSetting>(settings.scheduledDeliverySetting),
                            fromLong<IosNotificationSetting>(settings.timeSensitiveSetting)
                        )
                        println(json.encodeToString(iosNotificationSettings))
                        continuation.resume(iosNotificationSettings)
                    } else {
                        continuation.resumeWithException(Exception("Failed to retrieve notification settings"))
                    }
                }
        }
    }
}
