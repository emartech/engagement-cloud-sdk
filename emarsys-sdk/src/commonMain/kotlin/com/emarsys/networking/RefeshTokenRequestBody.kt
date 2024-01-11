package com.emarsys.networking

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequestBody(
    val refreshToken: String
)
