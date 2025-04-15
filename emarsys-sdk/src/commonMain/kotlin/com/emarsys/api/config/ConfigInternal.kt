package com.emarsys.api.config

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.language.LanguageHandlerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class ConfigInternal(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val uuidProvider: UuidProviderApi,
    private val timestampProvider: InstantProvider,
    private val sdkLogger: Logger,
    private val languageHandler: LanguageHandlerApi
): ConfigInstance {

    override suspend fun changeApplicationCode(applicationCode: String) {
        sdkLogger.debug("ConfigInternal - changeApplicationCode")
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.ChangeAppCode(
                uuidProvider.provide(),
                buildJsonObject {
                    put(
                        "applicationCode",
                        JsonPrimitive(applicationCode)
                    )
                },
                timestampProvider.provide()
            )
        )
    }

    override suspend fun changeMerchantId(merchantId: String) {
        sdkLogger.debug("ConfigInternal - changeMerchantId")
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.ChangeMerchantId(
                uuidProvider.provide(),
                buildJsonObject {
                    put(
                        "merchantId",
                        JsonPrimitive(merchantId)
                    )
                },
                timestampProvider.provide()
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