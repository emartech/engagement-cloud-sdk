package com.emarsys.core.providers

internal interface ApplicationVersionProviderApi: Provider<String> {
    override fun provide(): String
}