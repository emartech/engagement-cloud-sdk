package com.sap.ec.core.device

import kotlinx.serialization.Serializable

@Serializable
internal data class AndroidPlatformInfo(
    val osVersion: String,
    val notificationSettings: NotificationSettings?,
    val isDebugMode: Boolean
)
