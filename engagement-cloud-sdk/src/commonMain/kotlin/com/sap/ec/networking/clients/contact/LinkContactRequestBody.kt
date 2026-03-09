package com.sap.ec.networking.clients.contact

import kotlinx.serialization.Serializable

@Serializable
internal data class LinkContactRequestBody(
    val contactFieldValue: String? = null,
    val openIdToken: String? = null
)
