package com.sap.ec.remoteConfig

import kotlinx.serialization.Serializable

@Serializable
data class ServiceUrls(
    val eventService: String? = null,
    val clientService: String? = null,
    val deepLinkService: String? = null
)
