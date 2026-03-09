package com.sap.ec.core.language

internal interface LanguageTagValidatorApi {

    suspend fun isValid(languageTag: String): Boolean

}
