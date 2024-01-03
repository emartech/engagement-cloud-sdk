package com.emarsys.core.device

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInformation(
    val platform: String,
    val manufacturer: String,
    val displayMetrics: String,
    val model: String,
    val sdkVersion: String,
    val language: String,
    val timezone: String,
    val hardwareId: String,
    val platformInfo: String
)