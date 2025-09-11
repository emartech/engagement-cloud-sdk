package com.emarsys.context

data class DefaultUrls(
    override val clientServiceBaseUrl: String,
    override val eventServiceBaseUrl: String,
    override val deepLinkBaseUrl: String,
    override val remoteConfigBaseUrl: String,
    override val loggingUrl: String,
    override val embeddedMessagingBaseUrl: String
) : DefaultUrlsApi

fun DefaultUrlsApi.copyWith(
    clientServiceBaseUrl: String? = null,
    eventServiceBaseUrl: String? = null,
    deepLinkBaseUrl: String? = null,
    remoteConfigBaseUrl: String? = null,
    loggingUrl: String? = null,
    embeddedMessagingBaseUrl: String? = null
) = DefaultUrls(
    clientServiceBaseUrl = clientServiceBaseUrl ?: this.clientServiceBaseUrl,
    eventServiceBaseUrl = eventServiceBaseUrl ?: this.eventServiceBaseUrl,
    deepLinkBaseUrl = deepLinkBaseUrl ?: this.deepLinkBaseUrl,
    remoteConfigBaseUrl = remoteConfigBaseUrl ?: this.remoteConfigBaseUrl,
    loggingUrl = loggingUrl ?: this.loggingUrl,
    embeddedMessagingBaseUrl = embeddedMessagingBaseUrl ?: this.embeddedMessagingBaseUrl
)