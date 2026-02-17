package com.sap.ec.api.config

import com.sap.ec.core.device.notification.AndroidNotificationSettings

interface AndroidConfigApi {

    suspend fun getApplicationCode(): String?

    suspend fun getClientId(): String

    suspend fun getLanguageCode(): String

    suspend fun getApplicationVersion(): String

    suspend fun getSdkVersion(): String

    suspend fun changeApplicationCode(applicationCode: String): Result<Unit>

    suspend fun setLanguage(language: String): Result<Unit>

    suspend fun resetLanguage(): Result<Unit>

    suspend fun getNotificationSettings(): AndroidNotificationSettings
}