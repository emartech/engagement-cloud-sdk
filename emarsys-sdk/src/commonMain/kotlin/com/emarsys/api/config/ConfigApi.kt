package com.emarsys.api.config

import com.emarsys.core.device.NotificationSettings

interface ConfigApi {
    suspend fun getContactFieldId(): Int?
    suspend fun getApplicationCode(): String?
    suspend fun getMerchantId(): String?
    suspend fun getClientId(): String
    suspend fun getLanguageCode(): String
    suspend fun getSdkVersion(): String

    suspend fun changeApplicationCode(applicationCode: String): Result<Unit>

    suspend fun changeMerchantId(merchantId: String): Result<Unit>

    suspend fun setLanguage(language: String): Result<Unit>

    suspend fun resetLanguage(): Result<Unit>

    suspend fun getNotificationSettings(): NotificationSettings
}