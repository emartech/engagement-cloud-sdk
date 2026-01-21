package com.emarsys.api.config

import com.emarsys.core.device.notification.WebNotificationSettings

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSConfigApi {
    suspend fun getApplicationCode(): String?
    suspend fun getClientId(): String
    suspend fun getLanguageCode(): String
    suspend fun getApplicationVersion(): String
    suspend fun getSdkVersion(): String
    suspend fun getCurrentSdkState(): String
    suspend fun changeApplicationCode(applicationCode: String)
    suspend fun setLanguage(language: String)
    suspend fun resetLanguage()
    suspend fun getNotificationSettings(): WebNotificationSettings
}