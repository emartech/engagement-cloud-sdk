package com.emarsys.core.provider

import com.emarsys.core.providers.LanguageProviderApi
import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

internal class IosLanguageProvider: LanguageProviderApi {
    override fun provide(): String {
        return NSLocale.preferredLanguages()[0] as String
    }
}