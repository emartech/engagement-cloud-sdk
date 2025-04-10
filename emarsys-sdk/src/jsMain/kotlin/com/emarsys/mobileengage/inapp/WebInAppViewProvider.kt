package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.InstantProvider

internal class WebInAppViewProvider(
    private val inappScriptExtractor: InAppScriptExtractorApi,
    private val webInAppJsBridgeFactory: Factory<String, WebInAppJsBridge>,
    private val timestampProvider: InstantProvider,
) :
    InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        return WebInAppView(inappScriptExtractor, webInAppJsBridgeFactory, timestampProvider)
    }
}