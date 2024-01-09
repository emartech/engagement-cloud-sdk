package com.emarsys.networking.ktor.plugin

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequestBody(
    val refreshToken: String
)
