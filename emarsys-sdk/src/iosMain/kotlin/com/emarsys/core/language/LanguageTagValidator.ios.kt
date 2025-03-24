package com.emarsys.core.language

import platform.Foundation.ISOCountryCodes
import platform.Foundation.ISOLanguageCodes
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.languageCode

class LanguageTagValidator: LanguageTagValidatorApi {

    override suspend fun isValid(languageTag: String): Boolean {
        val locale = NSLocale(languageTag)
        return NSLocale.ISOLanguageCodes.contains(locale.languageCode) && NSLocale.ISOCountryCodes().contains(locale.countryCode)
    }
}