package com.sap.ec.core.language

internal interface LanguageHandlerApi {

    suspend fun handleLanguage(language: String?)

}