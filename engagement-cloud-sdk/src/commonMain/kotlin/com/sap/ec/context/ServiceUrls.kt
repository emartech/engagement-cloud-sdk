package com.sap.ec.context

import com.sap.ec.context.ServiceUrlsApi.FeatureUrlsApi
import com.sap.ec.context.ServiceUrlsApi.GlobalFeatureUrlsApi

internal data class ServiceUrls(
    override val remoteConfigBaseUrl: String,
    override var globalFeatureUrls: GlobalFeatureUrlsApi? = null,
    override var featureUrls: FeatureUrlsApi? = null
) : ServiceUrlsApi {

    data class GlobalFeatureUrls(
        override val deepLinkBaseUrl: String,
        override val jsBridgeUrl: String,
        override val jsBridgeSignatureUrl: String,
    ): GlobalFeatureUrlsApi

    data class FeatureUrls(
        override val clientServiceBaseUrl: String,
        override val eventServiceBaseUrl: String,
        override val embeddedMessagingBaseUrl: String,
        override val loggingUrl: String
    ): FeatureUrlsApi
}
