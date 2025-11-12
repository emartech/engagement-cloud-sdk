package com.emarsys.api.config

import com.emarsys.core.device.notification.WebNotificationSettings
import com.emarsys.core.device.notification.WebNotificationSettingsCollectorApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

internal class JSConfig(
    private val configApi: ConfigApi,
    private val webNotificationSettingsCollector: WebNotificationSettingsCollectorApi,
    private val applicationScope: CoroutineScope
) : JSConfigApi {

    override fun getApplicationCode(): Promise<String?> {
        return applicationScope.promise {
            configApi.getApplicationCode()
        }
    }

    override fun getClientId(): Promise<String> {
        return applicationScope.promise {
            configApi.getClientId()
        }
    }

    override fun getLanguageCode(): Promise<String> {
        return applicationScope.promise {
            configApi.getLanguageCode()
        }
    }

    override fun getApplicationVersion(): Promise<String> {
        return applicationScope.promise {
            configApi.getApplicationVersion()
        }
    }

    override fun getSdkVersion(): Promise<String> {
        return applicationScope.promise {
            configApi.getSdkVersion()
        }
    }

    override fun getCurrentSdkState(): Promise<String> {
        return applicationScope.promise {
            configApi.getCurrentSdkState().toJsSdkState()
        }
    }

    override fun changeApplicationCode(applicationCode: String): Promise<Unit> {
        return applicationScope.promise {
            configApi.changeApplicationCode(applicationCode).getOrThrow()
        }
    }

    override fun setLanguage(language: String): Promise<Unit> {
        return applicationScope.promise {
            configApi.setLanguage(language).getOrThrow()
        }
    }

    override fun resetLanguage(): Promise<Unit> {
        return applicationScope.promise {
            configApi.resetLanguage().getOrThrow()
        }
    }

    override fun getNotificationSettings(): Promise<WebNotificationSettings> {
        return applicationScope.promise {
            webNotificationSettingsCollector.collect()
        }
    }
}