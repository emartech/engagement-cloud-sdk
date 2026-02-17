package com.sap.ec.api.config

import com.sap.ec.config.ApplicationCode
import com.sap.ec.config.validate
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.collections.dequeue
import com.sap.ec.core.language.LanguageHandlerApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ConfigInternal(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val configContext: ConfigContextApi,
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

    override suspend fun setLanguage(language: String) {
        languageHandler.handleLanguage(language)
    }

    override suspend fun resetLanguage() {
        languageHandler.handleLanguage(null)
    }

    override suspend fun activate() {
        sdkLogger.debug("ConfigInternal - activate")
        configContext.calls.dequeue {
            when (it) {
                is ConfigCall.ChangeApplicationCode -> changeApplicationCode(it.applicationCode)
                is ConfigCall.SetLanguage -> setLanguage(it.language)
                is ConfigCall.ResetLanguage -> resetLanguage()
            }
        }
    }
}