package com.emarsys.api.config

import com.emarsys.config.ApplicationCode
import com.emarsys.config.validate
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.language.LanguageHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.event.SdkEvent

internal class ConfigInternal(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val uuidProvider: UuidProviderApi,
    private val timestampProvider: InstantProvider,
    private val sdkLogger: Logger,
    private val languageHandler: LanguageHandlerApi
) : ConfigInstance {

    override suspend fun changeApplicationCode(applicationCode: String) {
        val appCode = ApplicationCode(applicationCode.uppercase())
        appCode.validate(sdkLogger)
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.ChangeAppCode(
                id = uuidProvider.provide(),
                timestamp = timestampProvider.provide(),
                applicationCode = appCode.value
            )
        )
    }

    override suspend fun changeMerchantId(merchantId: String) {
        sdkLogger.debug("ConfigInternal - changeMerchantId")
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.ChangeMerchantId(
                id = uuidProvider.provide(),
                timestamp = timestampProvider.provide(),
                merchantId = merchantId
            )
        )
    }

    override suspend fun setLanguage(language: String) {
        languageHandler.handleLanguage(language)
    }

    override suspend fun resetLanguage() {
        languageHandler.handleLanguage(null)
    }

    override suspend fun activate() {
        sdkLogger.debug("ConfigInternal - activate")
    }
}