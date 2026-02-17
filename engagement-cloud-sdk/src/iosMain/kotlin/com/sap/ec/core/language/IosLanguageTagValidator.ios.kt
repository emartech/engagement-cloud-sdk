package com.sap.ec.core.language

import platform.Foundation.ISOCountryCodes
import platform.Foundation.ISOLanguageCodes
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.languageCode

internal class IosLanguageTagValidator: LanguageTagValidatorApi {

    override suspend fun isValid(languageTag: String): Boolean {
        val locale = NSLocale(languageTag)
        return NSLocale.ISOLanguageCodes.contains(locale.languageCode) && NSLocale.ISOCountryCodes().contains(locale.countryCode)
    }
}