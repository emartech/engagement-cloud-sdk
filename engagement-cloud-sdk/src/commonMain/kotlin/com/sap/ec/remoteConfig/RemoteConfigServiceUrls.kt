package com.sap.ec.remoteConfig

import kotlinx.serialization.Serializable

@Serializable
internal sealed interface RemoteConfigServiceUrls {
    @Serializable
    data class Global(
        val deepLinkService: String,
        val jsBridgeUrl: String,
        val jsBridgeSignatureUrl: String
    ) : RemoteConfigServiceUrls

    @Serializable
    data class AppCodeBased(
        val clientService: String,
        val eventService: String,
        val embeddedMessagingService: String,
        val loggingService: String
    ) : RemoteConfigServiceUrls

    @Serializable
    data class Optional(
        val clientService: String? = null,
        val eventService: String? = null,
        val embeddedMessagingService: String? = null,
        val loggingService: String? = null
    ) : RemoteConfigServiceUrls
}