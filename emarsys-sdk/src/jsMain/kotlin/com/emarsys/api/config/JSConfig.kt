package com.emarsys.api.config

import com.emarsys.core.device.NotificationSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

class JSConfig(private val configApi: ConfigApi, private val applicationScope: CoroutineScope) :
    JSConfigApi {
    override fun getContactFieldId(): Promise<Int?> {
        return applicationScope.promise {
            configApi.getContactFieldId()
        }
    }

    override fun getApplicationCode(): Promise<String?> {
        return applicationScope.promise {
            configApi.getApplicationCode()
        }
    }

    override fun getMerchantId(): Promise<String?> {
        return applicationScope.promise {
            configApi.getMerchantId()
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

    override fun getSdkVersion(): Promise<String> {
        return applicationScope.promise {
            configApi.getSdkVersion()
        }
    }

    override fun changeApplicationCode(applicationCode: String): Promise<Unit> {
        return applicationScope.promise {
            configApi.changeApplicationCode(applicationCode).getOrThrow()
        }
    }

    override fun changeMerchantId(merchantId: String): Promise<Unit> {
        return applicationScope.promise {
            configApi.changeMerchantId(merchantId).getOrThrow()
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

    override fun getPushSettings(): Promise<NotificationSettings> {
        return applicationScope.promise {
            configApi.getNotificationSettings()
        }
    }
}