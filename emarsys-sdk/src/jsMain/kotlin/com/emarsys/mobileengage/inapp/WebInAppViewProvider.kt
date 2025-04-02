package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory

internal class WebInAppViewProvider(
    private val inappScriptExtractor: InAppScriptExtractorApi,
    private val webInAppJsBridgeFactory: Factory<String, WebInAppJsBridge>
) :
    InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        return WebInAppView(inappScriptExtractor, webInAppJsBridgeFactory)
    }
}