package com.sap.ec.api.config

import com.sap.ec.core.device.notification.WebNotificationSettings
import com.sap.ec.core.device.notification.WebNotificationSettingsCollectorApi

internal class JSConfig(
    private val configApi: ConfigApi,
    private val webNotificationSettingsCollector: WebNotificationSettingsCollectorApi,
) : JSConfigApi {

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

    override suspend fun getCurrentSdkState(): String {
        return configApi.getCurrentSdkState().toJsSdkState()
    }

    override suspend fun changeApplicationCode(applicationCode: String) {
        configApi.changeApplicationCode(applicationCode).getOrThrow()
    }

    override suspend fun setLanguage(language: String) {
        configApi.setLanguage(language).getOrThrow()
    }

    override suspend fun resetLanguage() {
        configApi.resetLanguage().getOrThrow()
    }

    override suspend fun getNotificationSettings(): WebNotificationSettings {
        return webNotificationSettingsCollector.collect()
    }
}