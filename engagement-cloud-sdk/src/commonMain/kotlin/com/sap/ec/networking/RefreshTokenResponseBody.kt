package com.sap.ec.networking

import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshTokenResponseBody(
    val contactToken: String
)
