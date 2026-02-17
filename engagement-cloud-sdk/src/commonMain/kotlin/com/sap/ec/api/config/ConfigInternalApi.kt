package com.sap.ec.api.config

interface ConfigInternalApi {
    suspend fun changeApplicationCode(applicationCode: String)

    suspend fun setLanguage(language: String)

    suspend fun resetLanguage()
}