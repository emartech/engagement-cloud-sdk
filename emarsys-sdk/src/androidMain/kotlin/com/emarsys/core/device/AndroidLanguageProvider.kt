package com.emarsys.core.device

import com.emarsys.core.providers.LanguageProviderApi
import java.util.Locale

internal class AndroidLanguageProvider(private val locale: Locale) : LanguageProviderApi {
    override fun provide(): String {
        return locale.toLanguageTag()
    }
}
