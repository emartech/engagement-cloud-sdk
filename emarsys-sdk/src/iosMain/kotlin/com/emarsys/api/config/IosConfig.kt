package com.emarsys.api.config

import com.emarsys.core.device.PushSettings
import com.emarsys.di.SdkKoinIsolationContext.koin


class IosConfig : IosConfigApi {
    override suspend fun getContactFieldId(): Int? = koin.get<ConfigApi>().getContactFieldId()

    override suspend fun getApplicationCode(): String? = koin.get<ConfigApi>().getApplicationCode()

    override suspend fun getMerchantId(): String? = koin.get<ConfigApi>().getMerchantId()

    override suspend fun getClientId(): String = koin.get<ConfigApi>().getClientId()

    override suspend fun getLanguageCode(): String = koin.get<ConfigApi>().getLanguageCode()

    override suspend fun getSdkVersion(): String = koin.get<ConfigApi>().getSdkVersion()

    override suspend fun getPushSettings(): PushSettings = koin.get<ConfigApi>().getPushSettings()


    override suspend fun changeApplicationCode(applicationCode: String) {
        koin.get<ConfigApi>().changeApplicationCode(applicationCode).getOrThrow()
    }

    override suspend fun changeMerchantId(merchantId: String) {
        koin.get<ConfigApi>().changeMerchantId(merchantId).getOrThrow()
    }

    override suspend fun setLanguage(language: String) {
        koin.get<ConfigApi>().setLanguage(language).getOrThrow()
    }

    override suspend fun resetLanguage() {
        koin.get<ConfigApi>().resetLanguage().getOrThrow()
    }
}