package com.emarsys.core.device

import kotlinx.serialization.Serializable


data class DeviceInfo(
    val platform: String,
    val applicationVersion: String,
    val deviceModel: String,
    val manufacturer: String,
    val osVersion: String,
    val sdkVersion: String,
    val language: String,
    val timezone: String,
    val iOSPushSettings: PushSettings?,
    val androidNotificationSettings: NotificationSettings?,
    val hardwareId: String
)


@Serializable
data class PushSettings(
    val authorizationStatus: String,
    val soundSetting: String,
    val iOSBadgeSetting: String,
    val alertSetting: String,
    val notificationCenterSetting: String,
    val lockScreenSetting: String,
    val carPlaySetting: String,
    val alertStyle: String,
    val showPreviewsSetting: String,
    val criticalAlertSetting: String,
    val providesAppNotificationSettings: String,
    val scheduledDeliverySetting: String,
    val timeSensitiveSetting: String
)

interface NotificationSettings {
    val areNotificationsEnabled: Boolean
    val importance: Int
    val channelSettings: List<ChannelSettings>
}

data class ChannelSettings(
    val channelId: String,
    val importance: Int = -1000,
    val isCanBypassDnd: Boolean = false,
    val isCanShowBadge: Boolean = false,
    val isShouldVibrate: Boolean = false,
    val isShouldShowLights: Boolean = false
)