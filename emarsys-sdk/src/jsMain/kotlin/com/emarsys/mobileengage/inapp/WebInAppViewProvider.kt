package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi

internal class WebInAppViewProvider(
    private val inappScriptExtractor: InAppScriptExtractorApi,
    private val webInAppJsBridgeFactory: Factory<InAppJsBridgeData, WebInAppJsBridge>,
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi,
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        return WebInAppView(
            inappScriptExtractor,
            webInAppJsBridgeFactory,
            timestampProvider,
            uuidProvider
        )
    }
}