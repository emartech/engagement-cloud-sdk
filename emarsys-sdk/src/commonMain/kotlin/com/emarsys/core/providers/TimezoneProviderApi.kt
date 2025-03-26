package com.emarsys.core.providers

internal interface TimezoneProviderApi: Provider<String> {
    override fun provide(): String
}