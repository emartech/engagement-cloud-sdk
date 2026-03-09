package com.sap.ec.networking.clients.contact

import kotlinx.serialization.Serializable

@Serializable
internal data class ContactTokenResponseBody(
    val refreshToken: String,
    val contactToken: String
)
