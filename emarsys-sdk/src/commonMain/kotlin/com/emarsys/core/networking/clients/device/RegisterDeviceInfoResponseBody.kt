package com.emarsys.core.networking.clients.device

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceInfoResponseBody(
    val refreshToken: String,
    val contactToken: String
)
