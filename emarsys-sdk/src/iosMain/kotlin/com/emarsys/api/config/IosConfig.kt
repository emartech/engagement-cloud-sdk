package com.emarsys.api.config

import com.emarsys.core.device.notification.IosNotificationSettings
import com.emarsys.core.device.notification.IosNotificationSettingsCollectorApi

internal class IosConfig(
    private val configApi: ConfigApi,
    private val iosNotificationSettingsCollector: IosNotificationSettingsCollectorApi
) : IosConfigApi {
    override suspend fun getContactFieldId(): Int? = configApi.getContactFieldId()

    override suspend fun getApplicationCode(): String? = configApi.getApplicationCode()

    override suspend fun getClientId(): String = configApi.getClientId()

    override suspend fun getLanguageCode(): String = configApi.getLanguageCode()

    override suspend fun getApplicationVersion(): String =
        configApi.getApplicationVersion()

    override suspend fun getSdkVersion(): String = configApi.getSdkVersion()

    override suspend fun getNotificationSettings(): IosNotificationSettings {
        return iosNotificationSettingsCollector.collect()
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
}