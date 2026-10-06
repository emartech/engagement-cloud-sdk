package com.sap.ec.remoteConfig

import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteConfigGlobalServiceUrls(
    val deepLinkService: String,
    val jsBridgeUrl: String,
    val jsBridgeSignatureUrl: String
)

@Serializable
internal data class RemoteConfigServiceUrls(
    val clientService: String,
    val eventService: String,
    val embeddedMessagingService: String,
    val loggingService: String
)

@Serializable
internal data class RemoteConfigOptionalServiceUrls(
    val clientService: String? = null,
    val eventService: String? = null,
    val embeddedMessagingService: String? = null,
    val loggingService: String? = null
)