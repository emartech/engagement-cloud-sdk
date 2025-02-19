package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory

class WebInAppViewProvider(
    private val inappScriptExtractor: InAppScriptExtractorApi,
    private val inAppJsBridgeFactory: Factory<String, InAppJsBridge>
) :
    InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        return WebInAppView(inappScriptExtractor, inAppJsBridgeFactory)
    }
}