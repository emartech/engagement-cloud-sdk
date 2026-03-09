package com.sap.ec.core.device

import kotlinx.serialization.Serializable

@Serializable
internal data class WebPlatformInfo(
    val notificationSettings: String?,
    val isDebugMode: Boolean,
    val osName: String,
    val osVersion: String,
    val browserName: String,
    val browserVersion: String
)