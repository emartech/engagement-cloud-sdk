package com.emarsys.core.device

import java.util.Locale

class AndroidLanguageProvider(private val locale: Locale) : LanguageProvider {
    override fun provideLanguage(): String {
        return locale.toLanguageTag()
    }
}
