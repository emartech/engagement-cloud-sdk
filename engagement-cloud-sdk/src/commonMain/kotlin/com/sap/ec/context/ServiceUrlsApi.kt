package com.sap.ec.context

internal interface ServiceUrlsApi {
    val remoteConfigBaseUrl: String
    var globalFeatureUrls: GlobalFeatureUrlsApi?
    var featureUrls: FeatureUrlsApi?

    interface GlobalFeatureUrlsApi {
        val deepLinkBaseUrl: String
        val jsBridgeUrl: String
        val jsBridgeSignatureUrl: String
    }

    interface FeatureUrlsApi {
        val clientServiceBaseUrl: String
        val eventServiceBaseUrl: String
        val embeddedMessagingBaseUrl: String
        val loggingUrl: String
    }
}