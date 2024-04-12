package com.emarsys.core.device

import kotlinx.serialization.Serializable

@Serializable
data class AndroidPlatformInfo(
    val osVersion: String,
    val notificationSettings: PushSettings?,
    val isDebugMode: Boolean
)
