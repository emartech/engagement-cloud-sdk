package com.emarsys.core.language

interface LanguageTagValidatorApi {

    suspend fun isValid(languageTag: String): Boolean

}
