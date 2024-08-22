package com.emarsys.core.provider

import com.emarsys.core.providers.Provider
import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

class IosLanguageProvider: Provider<String> {
    override fun provide(): String {
        return NSLocale.preferredLanguages()[0] as String
    }
}