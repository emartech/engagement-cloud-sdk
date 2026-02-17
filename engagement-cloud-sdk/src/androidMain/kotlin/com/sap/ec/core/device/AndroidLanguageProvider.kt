package com.sap.ec.core.device

import com.sap.ec.core.providers.LanguageProviderApi
import java.util.Locale

internal class AndroidLanguageProvider(private val locale: Locale) : LanguageProviderApi {
    override fun provide(): String {
        return locale.toLanguageTag()
    }
}
