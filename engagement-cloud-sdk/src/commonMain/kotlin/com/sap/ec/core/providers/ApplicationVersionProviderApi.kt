package com.sap.ec.core.providers

internal interface ApplicationVersionProviderApi: Provider<String> {
    override fun provide(): String
}