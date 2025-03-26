package com.emarsys.core.providers

internal interface LanguageProviderApi: Provider<String> {
    override fun provide(): String
}