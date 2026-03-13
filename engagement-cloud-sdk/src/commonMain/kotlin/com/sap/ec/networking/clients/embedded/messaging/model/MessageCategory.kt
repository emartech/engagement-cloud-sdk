package com.sap.ec.networking.clients.embedded.messaging.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageCategory(
    val id: String,
    val value: String
)
