package com.emarsys.core.provider

import com.emarsys.core.providers.Provider

class ApplicationVersionProvider: Provider<String> {
    private companion object {
        const val UNKNOWN_VERSION = "0.0.0"
    }

    override fun provide(): String {
        return UNKNOWN_VERSION
    }
}