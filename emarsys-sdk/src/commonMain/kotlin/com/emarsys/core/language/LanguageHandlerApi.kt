package com.emarsys.core.language

internal interface LanguageHandlerApi {

    suspend fun handleLanguage(language: String?)

}