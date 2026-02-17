package com.sap.ec.api.config

import com.sap.ec.core.device.notification.AndroidNotificationSettings
import com.sap.ec.core.device.notification.AndroidNotificationSettingsCollectorApi

internal class AndroidConfig(
    private val configApi: ConfigApi,
    private val androidNotificationSettingsCollector: AndroidNotificationSettingsCollectorApi
) : AndroidConfigApi {

    override suspend fun getApplicationCode(): String? {
        return configApi.getApplicationCode()
    }

    override suspend fun getClientId(): String {
        return configApi.getClientId()
    }

    override suspend fun getLanguageCode(): String {
        return configApi.getLanguageCode()
    }

    override suspend fun getApplicationVersion(): String {
        return configApi.getApplicationVersion()
    }

    override suspend fun getSdkVersion(): String {
        return configApi.getSdkVersion()
    }

    override suspend fun changeApplicationCode(applicationCode: String): Result<Unit> {
        return configApi.changeApplicationCode(applicationCode)
    }

    override suspend fun setLanguage(language: String): Result<Unit> {
        return configApi.setLanguage(language)
    }

    override suspend fun resetLanguage(): Result<Unit> {
        return configApi.resetLanguage()
    }

    override suspend fun getNotificationSettings(): AndroidNotificationSettings {
        return androidNotificationSettingsCollector.collect()
    }
}