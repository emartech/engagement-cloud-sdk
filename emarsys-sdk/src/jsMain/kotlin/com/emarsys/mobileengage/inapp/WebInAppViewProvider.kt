package com.emarsys.mobileengage.inapp

class WebInAppViewProvider(private val inappScriptExtractor: InAppScriptExtractorApi) :
    InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        return WebInAppView(inappScriptExtractor)
    }
}