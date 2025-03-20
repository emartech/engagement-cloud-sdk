package com.emarsys.api.config

import com.emarsys.core.device.PushSettings

interface ConfigApi {
    suspend fun getContactFieldId(): Int?
    suspend fun getApplicationCode(): String?
    suspend fun getMerchantId(): String?
    suspend fun getClientId(): String
    suspend fun getLanguageCode(): String
    suspend fun getSdkVersion(): String
    suspend fun changeApplicationCode(applicationCode: String): Result<Unit>
    suspend fun changeMerchantId(merchantId: String): Result<Unit>
    suspend fun getPushSettings(): PushSettings
}