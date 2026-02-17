package com.sap.ec.networking

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponseBody(
    val contactToken: String
)
