package com.sap.ec.api.config

import com.sap.ec.InternalSdkApi

@InternalSdkApi
interface ConfigInternalApi {
    suspend fun changeApplicationCode(applicationCode: String)

    suspend fun setLanguage(language: String)

    suspend fun resetLanguage()
}