package com.sap.ec.context

internal data class ServiceUrls(
    override val clientServiceBaseUrl: String,
    override val eventServiceBaseUrl: String,
    override val deepLinkBaseUrl: String,
    override val remoteConfigBaseUrl: String,
    override val loggingUrl: String,
    override val embeddedMessagingBaseUrl: String,
    override val jsBridgeUrl: String,
    override val jsBridgeSignatureUrl: String
) : ServiceUrlsApi

internal fun ServiceUrlsApi.copyWith(
    clientServiceBaseUrl: String? = null,
    eventServiceBaseUrl: String? = null,
    deepLinkBaseUrl: String? = null,
    remoteConfigBaseUrl: String? = null,
    loggingUrl: String? = null,
    embeddedMessagingBaseUrl: String? = null,
    jsBridgeUrl: String? = null,
    jsBridgeSignatureUrl: String? = null
) = ServiceUrls(
    clientServiceBaseUrl = clientServiceBaseUrl ?: this.clientServiceBaseUrl,
    eventServiceBaseUrl = eventServiceBaseUrl ?: this.eventServiceBaseUrl,
    deepLinkBaseUrl = deepLinkBaseUrl ?: this.deepLinkBaseUrl,
    remoteConfigBaseUrl = remoteConfigBaseUrl ?: this.remoteConfigBaseUrl,
    loggingUrl = loggingUrl ?: this.loggingUrl,
    embeddedMessagingBaseUrl = embeddedMessagingBaseUrl ?: this.embeddedMessagingBaseUrl,
    jsBridgeUrl = jsBridgeUrl ?: this.jsBridgeUrl,
    jsBridgeSignatureUrl = jsBridgeSignatureUrl ?: this.jsBridgeSignatureUrl
)