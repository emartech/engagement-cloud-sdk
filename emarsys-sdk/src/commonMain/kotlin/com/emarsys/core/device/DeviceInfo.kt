package com.emarsys.core.device

import kotlinx.serialization.Serializable

const val UNKNOWN_VERSION_NAME = "unknown"

@Serializable
data class DeviceInfo(
    val platform: String,
    val platformCategory: String,
    val platformWrapper: String?,
    val platformWrapperVersion: String?,
    val applicationVersion: String,
    val deviceModel: String,
    val osVersion: String,
    val sdkVersion: String,
    val language: String,
    val timezone: String,
    val clientId: String
)

@Serializable
data class DeviceInfoForLogs(
    val platform: String,
    val platformCategory: String,
    val platformWrapper: String?,
    val platformWrapperVersion: String?,
    val applicationVersion: String,
    val deviceModel: String,
    val osVersion: String,
    val sdkVersion: String,
    val isDebugMode: Boolean,
    val applicationCode: String?,
    val merchantId: String?,
    val language: String,
    val timezone: String,
    val clientId: String
)

@Serializable
sealed interface PushSettings

@Serializable
data class AndroidNotificationSettings(
    val areNotificationsEnabled: Boolean,
    val importance: Int,
    val channelSettings: List<ChannelSettings>
): PushSettings

@Serializable
data class IosNotificationSettings(
    val authorizationStatus: IosAuthorizationStatus,
    val soundSetting: IosNotificationSetting,
    val badgeSetting: IosNotificationSetting,
    val alertSetting: IosNotificationSetting,
    val notificationCenterSetting: IosNotificationSetting,
    val lockScreenSetting: IosNotificationSetting,
    val carPlaySetting: IosNotificationSetting,
    val alertStyle: IosAlertStyle,
    val showPreviewsSetting: IosShowPreviewSetting,
    val criticalAlertSetting: IosNotificationSetting,
    val providesAppNotificationSettings: Boolean = false,
    // Last 2 property is only available from iOS 15.0
    val scheduledDeliverySetting: IosNotificationSetting,
    val timeSensitiveSetting: IosNotificationSetting
): PushSettings

@Serializable
data class ChannelSettings(
    val channelId: String,
    val importance: Int = -1000,
    val canBypassDnd: Boolean = false,
    val canShowBadge: Boolean = false,
    val shouldVibrate: Boolean = false,
    val shouldShowLights: Boolean = false
)
