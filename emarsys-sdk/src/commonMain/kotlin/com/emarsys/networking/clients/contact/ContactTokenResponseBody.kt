package com.emarsys.networking.clients.contact

import kotlinx.serialization.Serializable

@Serializable
data class ContactTokenResponseBody(
    val refreshToken: String,
    val contactToken: String
)
