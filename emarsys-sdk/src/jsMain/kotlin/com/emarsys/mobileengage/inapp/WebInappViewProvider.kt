package com.emarsys.mobileengage.inapp

class WebInappViewProvider(private val inappScriptExtractor: InappScriptExtractorApi) :
    InAppViewProviderApi {
    override fun provide(): InAppViewApi {
        return WebInappView(inappScriptExtractor)
    }
}