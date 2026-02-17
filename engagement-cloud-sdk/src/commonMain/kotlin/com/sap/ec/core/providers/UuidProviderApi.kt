package com.sap.ec.core.providers

internal interface UuidProviderApi: Provider<String> {
    override fun provide(): String
}