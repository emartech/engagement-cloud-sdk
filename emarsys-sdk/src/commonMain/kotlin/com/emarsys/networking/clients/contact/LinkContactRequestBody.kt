package com.emarsys.networking.clients.contact

import kotlinx.serialization.Serializable

@Serializable
data class LinkContactRequestBody(
    val contactFieldValue: String? = null,
    val openIdToken: String? = null
)
