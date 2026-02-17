package com.sap.ec.api.config

import com.sap.ec.api.SdkState
import com.sap.ec.core.device.notification.IosNotificationSettings
import kotlin.coroutines.cancellation.CancellationException

interface IosConfigApi {
    suspend fun getApplicationCode(): String?
    suspend fun getClientId(): String
    suspend fun getLanguageCode(): String
    suspend fun getApplicationVersion(): String
    suspend fun getSdkVersion(): String
    suspend fun getCurrentSdkState(): SdkState
    suspend fun getNotificationSettings(): IosNotificationSettings

    @Throws(CancellationException::class)
    suspend fun changeApplicationCode(applicationCode: String)

    @Throws(CancellationException::class)
    suspend fun setLanguage(language: String)

    @Throws(CancellationException::class)
    suspend fun resetLanguage()
}