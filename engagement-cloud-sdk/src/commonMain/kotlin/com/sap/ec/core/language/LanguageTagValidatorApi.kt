package com.sap.ec.core.language

interface LanguageTagValidatorApi {

    suspend fun isValid(languageTag: String): Boolean

}
