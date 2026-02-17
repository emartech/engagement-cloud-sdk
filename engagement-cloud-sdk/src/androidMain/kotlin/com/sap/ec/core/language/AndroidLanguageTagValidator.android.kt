package com.sap.ec.core.language

import java.util.Locale

internal class AndroidLanguageTagValidator: LanguageTagValidatorApi {

    override suspend fun isValid(languageTag: String): Boolean {
        val locale = Locale.forLanguageTag(languageTag)
        return Locale.getISOLanguages().contains(locale.language) && Locale.getISOCountries().contains(locale.country)
    }
}
