package com.sap.ec.context

internal interface DefaultUrlsApi {
    val clientServiceBaseUrl: String
    val eventServiceBaseUrl: String
    val deepLinkBaseUrl: String
    val remoteConfigBaseUrl: String
    val loggingUrl: String
    val embeddedMessagingBaseUrl: String
    val jsBridgeUrl: String
    val jsBridgeSignatureUrl: String
}