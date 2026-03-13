package com.sap.ec.networking.clients.embedded.messaging.model

import kotlinx.serialization.Serializable

@Serializable
internal data class MessageCategory(
    val id: String,
    val value: String
)

internal fun MessageCategory.toCategory(): Category = Category(id, value)
