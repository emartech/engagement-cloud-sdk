package com.sap.ec.networking

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequestBody(
    val refreshToken: String
)
