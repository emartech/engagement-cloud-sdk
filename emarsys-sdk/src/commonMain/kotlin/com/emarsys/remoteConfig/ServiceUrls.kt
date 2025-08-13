package com.emarsys.remoteConfig

import kotlinx.serialization.Serializable

@Serializable
data class ServiceUrls(
    val eventService: String? = null,
    val clientService: String? = null,
    val deepLinkService: String? = null,
    val inboxService: String? = null
)
