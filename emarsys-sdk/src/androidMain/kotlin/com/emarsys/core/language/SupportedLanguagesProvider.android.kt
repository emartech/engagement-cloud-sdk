package com.emarsys.core.language

import com.emarsys.core.providers.Provider
import java.util.Locale

class SupportedLanguagesProvider: Provider<List<String>> {

    override fun provide(): List<String> {
        return Locale.getAvailableLocales().map { it.language }
    }
}
