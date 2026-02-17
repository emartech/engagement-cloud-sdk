package com.sap.ec.core.providers

internal class WebApplicationVersionProvider: ApplicationVersionProviderApi {
    private companion object {
        const val UNKNOWN_VERSION = "0.0.0"
    }

    override fun provide(): String {
        return UNKNOWN_VERSION
    }
}