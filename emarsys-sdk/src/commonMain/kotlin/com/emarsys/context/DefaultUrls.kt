package com.emarsys.context

data class DefaultUrls(
    override val clientServiceBaseUrl: String,
    override val eventServiceBaseUrl: String,
    override val predictBaseUrl: String,
    override val deepLinkBaseUrl: String,
    override val inboxBaseUrl: String,
    override val remoteConfigBaseUrl: String,
    override val loggingUrl: String
) : DefaultUrlsApi

fun DefaultUrlsApi.copyWith(
    clientServiceBaseUrl: String? = null,
    eventServiceBaseUrl: String? = null,
    predictBaseUrl: String? = null,
    deepLinkBaseUrl: String? = null,
    inboxBaseUrl: String? = null,
    remoteConfigBaseUrl: String? = null,
    loggingUrl: String? = null
) = DefaultUrls(
    clientServiceBaseUrl = clientServiceBaseUrl ?: this.clientServiceBaseUrl,
    eventServiceBaseUrl = eventServiceBaseUrl ?: this.eventServiceBaseUrl,
    predictBaseUrl = predictBaseUrl ?: this.predictBaseUrl,
    deepLinkBaseUrl = deepLinkBaseUrl ?: this.deepLinkBaseUrl,
    inboxBaseUrl = inboxBaseUrl ?: this.inboxBaseUrl,
    remoteConfigBaseUrl = remoteConfigBaseUrl ?: this.remoteConfigBaseUrl,
    loggingUrl = loggingUrl ?: this.loggingUrl
)