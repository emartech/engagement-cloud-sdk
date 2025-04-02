package com.emarsys.core.language

internal class WebLanguageTagValidator: LanguageTagValidatorApi {

    override suspend fun isValid(languageTag: String): Boolean {
        return try {
            val locale = js("new Intl.Locale(languageTag)")
            val language = locale.language as String
            val region = locale.region as String?

            val languages = js("new Intl.DisplayNames(['en'], { type: 'language' })")
            val regions = js("new Intl.DisplayNames(['en'], { type: 'region' })")

            val foundLanguage = languages.of(language)
            val validLanguage = foundLanguage != undefined && foundLanguage != language
            val foundRegion = regions.of(region)
            val validRegion = foundRegion != undefined && foundRegion != region

            validLanguage && validRegion
        } catch (e: dynamic) {
            false
        }
    }
}
