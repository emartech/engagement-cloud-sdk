package com.emarsys.networking

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponseBody(
    val contactToken: String
)
