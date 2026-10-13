package com.sap.ec.context

internal data class ServiceUrls(
    val remoteConfigBaseUrl: String,
    var globalServiceUrls: Global? = null,
    var appCodeBasedServiceUrls: AppCodeBased? = null
) {
    internal data class Global(
        val deepLinkBaseUrl: String,
        val jsBridgeUrl: String,
        val jsBridgeSignatureUrl: String,
    )

    internal data class AppCodeBased(
        val clientServiceBaseUrl: String,
        val eventServiceBaseUrl: String,
        val embeddedMessagingBaseUrl: String,
        val loggingUrl: String
    )
}
