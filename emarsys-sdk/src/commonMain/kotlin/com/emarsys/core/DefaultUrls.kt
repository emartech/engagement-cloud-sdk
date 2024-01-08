package com.emarsys.core

data class DefaultUrls(
    override val clientServiceBaseUrl: String,
    override val eventServiceBaseUrl: String,
    override val predictBaseUrl: String,
    override val deepLinkBaseUrl: String,
    override val inboxBaseUrl: String,
    override val remoteConfigBaseUrl: String,
    override val loggingUrl: String
) : DefaultUrlsApi