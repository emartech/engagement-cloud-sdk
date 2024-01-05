package com.emarsys.core.device

import kotlinx.serialization.Serializable

@Serializable
data class AndroidPlatformInfo(
    val applicationVersion: String,
    val osVersion: String,
    val notificationSettings: NotificationSettings?,
    val isDebugMode: Boolean
)
