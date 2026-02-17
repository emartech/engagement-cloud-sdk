package com.sap.ec.core.device

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
    val language: String,
    val timezone: String,
    val clientId: String
)

@Serializable
data class NotificationSettings(
    val areNotificationsEnabled: Boolean
)

@Serializable
data class ChannelSettings(
    val channelId: String,
    val importance: Int = -1000,
    val canBypassDnd: Boolean = false,
    val canShowBadge: Boolean = false,
    val shouldVibrate: Boolean = false,
    val shouldShowLights: Boolean = false
)
