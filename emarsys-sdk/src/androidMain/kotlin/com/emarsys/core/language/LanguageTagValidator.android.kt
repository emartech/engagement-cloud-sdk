package com.emarsys.core.language

import java.util.Locale

class LanguageTagValidator: LanguageTagValidatorApi {

    override suspend fun isValid(languageTag: String): Boolean {
        val locale = Locale.forLanguageTag(languageTag)
        return Locale.getISOLanguages().contains(locale.language) && Locale.getISOCountries().contains(locale.country)
    }
}
