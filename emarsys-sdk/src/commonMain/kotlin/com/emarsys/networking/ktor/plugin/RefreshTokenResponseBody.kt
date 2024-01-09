package com.emarsys.networking.ktor.plugin

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponseBody(
    val contactToken: String
)
