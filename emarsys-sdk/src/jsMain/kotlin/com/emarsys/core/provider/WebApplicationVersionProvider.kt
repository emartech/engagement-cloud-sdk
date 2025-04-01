package com.emarsys.core.provider

import com.emarsys.core.providers.ApplicationVersionProviderApi

internal class WebApplicationVersionProvider: ApplicationVersionProviderApi {
    private companion object {
        const val UNKNOWN_VERSION = "0.0.0"
    }

    override fun provide(): String {
        return UNKNOWN_VERSION
    }
}