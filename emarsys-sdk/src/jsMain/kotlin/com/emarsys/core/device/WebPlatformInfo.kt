package com.emarsys.core.device

import kotlinx.serialization.Serializable

@Serializable
data class WebPlatformInfo(
    val notificationSettings: String?,
    val isDebugMode: Boolean,
    val osName: String,
    val osVersion: String,
    val browserName: String,
    val browserVersion: String
)