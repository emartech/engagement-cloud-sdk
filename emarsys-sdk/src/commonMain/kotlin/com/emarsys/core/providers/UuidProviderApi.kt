package com.emarsys.core.providers

internal interface UuidProviderApi: Provider<String> {
    override fun provide(): String
}