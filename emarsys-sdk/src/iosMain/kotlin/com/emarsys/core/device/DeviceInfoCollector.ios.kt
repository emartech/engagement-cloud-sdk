package com.emarsys.core.device

import com.emarsys.KotlinPlatform
import com.emarsys.SdkConstants
import com.emarsys.core.device.IosNotificationConstant.Companion.fromLong
import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.wrapper.WrapperInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class DeviceInfoCollector(
    private val clientIdProvider: Provider<String>,
    private val applicationVersionProvider: Provider<String>,
    private val languageProvider: Provider<String>,
    private val timezoneProvider: Provider<String>,
    private val deviceInformation: UIDeviceApi,
    private val wrapperInfoStorage: TypedStorageApi,
    private val json: Json
    private val wrapperInfoStorage: TypedStorageApi<WrapperInfo?>,
    private val json: Json,
    private val stringStorage: StringStorageApi
) : DeviceInfoCollectorApi {
    actual override suspend fun collect(): String {
        val deviceInfo = DeviceInfo(
            KotlinPlatform.IOS.name.lowercase(),
            platformCategory = SdkConstants.MOBILE_PLATFORM_CATEGORY,
            platformWrapper = getWrapperInfo()?.platformWrapper,
            platformWrapperVersion = getWrapperInfo()?.wrapperVersion,
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = deviceInformation.deviceModel(),
            osVersion = deviceInformation.osVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            language = stringStorage.get(SdkConstants.LANGUAGE_STORAGE_KEY) ?: languageProvider.provide(),
            timezone = timezoneProvider.provide(),
            clientId = clientIdProvider.provide()
        )
        return json.encodeToString(deviceInfo)
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
