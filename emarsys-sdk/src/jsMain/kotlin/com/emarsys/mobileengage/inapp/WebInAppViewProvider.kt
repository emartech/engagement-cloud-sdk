package com.emarsys.mobileengage.inapp

class WebInAppViewProvider(private val inappScriptExtractor: InAppScriptExtractorApi) :
    InAppViewProviderApi {
    override fun provide(): InAppViewApi {
        return WebInAppView(inappScriptExtractor)
    }
}