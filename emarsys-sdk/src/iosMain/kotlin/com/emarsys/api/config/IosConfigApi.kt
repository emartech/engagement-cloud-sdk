package com.emarsys.api.config

import com.emarsys.core.device.IosNotificationSettings
import kotlin.coroutines.cancellation.CancellationException

interface IosConfigApi {
    suspend fun getContactFieldId(): Int?
    suspend fun getApplicationCode(): String?
    suspend fun getClientId(): String
    suspend fun getLanguageCode(): String
    suspend fun getApplicationVersion(): String
    suspend fun getSdkVersion(): String
    suspend fun getNotificationSettings(): IosNotificationSettings

    @Throws(CancellationException::class)
    suspend fun changeApplicationCode(applicationCode: String)

    @Throws(CancellationException::class)
    suspend fun setLanguage(language: String)

    @Throws(CancellationException::class)
    suspend fun resetLanguage()
}