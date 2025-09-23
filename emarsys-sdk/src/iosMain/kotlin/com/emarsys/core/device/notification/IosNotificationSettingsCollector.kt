package com.emarsys.core.device.notification

import com.emarsys.core.device.IosAlertStyle
import com.emarsys.core.device.IosAuthorizationStatus
import com.emarsys.core.device.IosNotificationConstant.Companion.fromLong
import com.emarsys.core.device.IosNotificationSetting
import com.emarsys.core.device.toShowPreviewSetting
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IosNotificationSettingsCollector(
    private val json: Json
) : IosNotificationSettingsCollectorApi {

    override suspend fun collect(): IosNotificationSettings {
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