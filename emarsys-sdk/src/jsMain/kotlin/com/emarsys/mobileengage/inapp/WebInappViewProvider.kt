package com.emarsys.mobileengage.inApp

class WebInappViewProvider(private val inappScriptExtractor: InappScriptExtractorApi): InAppViewProviderApi {
    override fun provide(): InAppViewApi {
        return WebInappView(inappScriptExtractor)
    }
}