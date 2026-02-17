package com.sap.ec.core.device

import kotlinx.serialization.Serializable

@Serializable
data class AndroidPlatformInfo(
    val osVersion: String,
    val notificationSettings: NotificationSettings?,
    val isDebugMode: Boolean
)
