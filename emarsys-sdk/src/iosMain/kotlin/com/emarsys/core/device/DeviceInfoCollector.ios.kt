package com.emarsys.core.device

import com.emarsys.KotlinPlatform
import com.emarsys.core.device.IosNotificationConstant.Companion.fromLong
import com.emarsys.core.providers.Provider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class DeviceInfoCollector(
    private val hardwareIdProvider: Provider<String>,
    private val applicationVersionProvider: Provider<String>,
    private val languageProvider: Provider<String>,
    private val timezoneProvider: Provider<String>,
    private val deviceInformation: UIDeviceApi,
    private val json: Json
) : DeviceInfoCollectorApi {
    actual override fun collect(): String {
        val deviceInfo = DeviceInfo(
            KotlinPlatform.IOS.name,
            applicationVersion = applicationVersionProvider.provide(),
            deviceModel = deviceInformation.deviceModel(),
            osVersion = deviceInformation.osVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            languageCode = languageProvider.provide(),
            timezone = timezoneProvider.provide()
        )
        return json.encodeToString(deviceInfo)
    }

    actual override fun getHardwareId(): String {
        return hardwareIdProvider.provide()
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
