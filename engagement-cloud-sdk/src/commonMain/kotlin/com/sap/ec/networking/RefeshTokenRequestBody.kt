package com.sap.ec.networking

import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshTokenRequestBody(
    val refreshToken: String
)
