package com.sap.ec.remoteConfig

import com.sap.ec.context.Features
import com.sap.ec.context.Features.EmbeddedMessaging
import com.sap.ec.context.Features.JsBridgeSignatureCheck
import com.sap.ec.context.Features.MobileEngage
import com.sap.ec.context.SdkContextApi
import com.sap.ec.context.ServiceUrls
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.log.LogConfigHolderApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.DoubleProvider
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import com.sap.ec.core.exceptions.SdkException.SdkDisabledException

internal class RemoteConfigHandler(
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val logConfigHolder: LogConfigHolderApi,
    private val sdkContext: SdkContextApi,
    private val randomProvider: DoubleProvider,
    private val embeddedMessagingContext: EmbeddedMessagingContextApi,
    private val sdkLogger: Logger
) : RemoteConfigHandlerApi {
    override suspend fun handle(remoteConfig: RemoteConfig) {
        if (remoteConfig is RemoteConfig.Default) {
            handleDefault(remoteConfig)
        } else if (remoteConfig is RemoteConfig.Response.Global) {
            handleGlobalResponse(remoteConfig)
        } else if (remoteConfig is RemoteConfig.Response.AppCodeBased) {
            handleAppCodeBasedResponse(remoteConfig)
        }
    }

    private suspend fun handleDefault(remoteConfig: RemoteConfig.Default) {
        sdkContext.serviceUrls.globalServiceUrls = null
        sdkContext.serviceUrls.appCodeBasedServiceUrls = null

        applyLogLevel(remoteConfig.logLevel, remoteConfig.luckyLogger)
        applyFeatures(remoteConfig.features)
        applyEmbeddedMessagingConfig(remoteConfig.embeddedMessagingConfig)
    }

    private suspend fun handleGlobalResponse(remoteConfig: RemoteConfig.Response.Global) {
        applyGlobalRemoteConfigApplicationCodeValidationRegex(remoteConfig.globalRemoteConfigApplicationCodeValidationRegex)
        applyGlobalServiceUrls(remoteConfig.globalServiceUrls)

        applyLogLevel(remoteConfig.logLevel, remoteConfig.luckyLogger)
        applyFeatures(remoteConfig.features)
        applyEmbeddedMessagingConfig(remoteConfig.embeddedMessagingConfig)
    }

    private suspend fun handleAppCodeBasedResponse(remoteConfig: RemoteConfig.Response.AppCodeBased) {
        val clientId = deviceInfoCollector.getClientId()
        val override = remoteConfig.overrides?.get(clientId)

        applyAppCodeBasedServiceUrls(remoteConfig.appCodeBasedServiceUrls, override?.serviceUrls)

        applyLogLevel(remoteConfig.logLevel, remoteConfig.luckyLogger, override?.logLevel)
        applyFeatures(remoteConfig.features, override?.features, remoteConfig.disabled)
        applyEmbeddedMessagingConfig(remoteConfig.embeddedMessagingConfig, override?.embeddedMessagingConfig)
    }

    private suspend fun applyGlobalRemoteConfigApplicationCodeValidationRegex(regex: String?) {
        regex?.let { regexString ->
            sdkLogger.debug("applyGlobalRemoteConfigApplicationCodeValidationRegex")
            sdkContext.globalRemoteConfigApplicationCodeValidationRegex = try {
                regexString.toRegex()
            } catch (exception: Throwable) {
                currentCoroutineContext().ensureActive()
                sdkLogger.error(
                    "Invalid sdkManagementApplicationCodeValidationRegex: $regexString",
                    exception
                )
                null
            }
        }
    }

    private suspend fun applyGlobalServiceUrls(globalServiceUrls: RemoteConfigServiceUrls.Global) {
        sdkLogger.debug("applyGlobalServiceUrls")
        sdkContext.serviceUrls.globalServiceUrls = ServiceUrls.Global(
            deepLinkBaseUrl = globalServiceUrls.deepLinkService,
            jsBridgeUrl = globalServiceUrls.jsBridgeUrl,
            jsBridgeSignatureUrl = globalServiceUrls.jsBridgeSignatureUrl
        )
    }

    private suspend fun applyAppCodeBasedServiceUrls(appCodeBasedServiceUrls: RemoteConfigServiceUrls.AppCodeBased, override: RemoteConfigServiceUrls.Optional?) {
        sdkLogger.debug("applyAppCodeBasedServiceUrls ${override?.let { "with override" } ?: ""}")
        sdkContext.serviceUrls.appCodeBasedServiceUrls = ServiceUrls.AppCodeBased(
            clientServiceBaseUrl = override?.clientService ?: appCodeBasedServiceUrls.clientService,
            eventServiceBaseUrl = override?.eventService ?: appCodeBasedServiceUrls.eventService,
            embeddedMessagingBaseUrl = override?.embeddedMessagingService ?: appCodeBasedServiceUrls.embeddedMessagingService,
            loggingUrl = override?.loggingService ?: appCodeBasedServiceUrls.loggingService
        )
    }

    private suspend fun applyLogLevel(logLevel: LogLevel?, luckyLogger: LuckyLogger?, override: LogLevel? = null) {
        sdkLogger.debug("applyLogLevel ${override?.let { "with override" } ?: ""}")
        (override ?: getLuckyLogLevel(luckyLogger) ?: logLevel)?.let {
            logConfigHolder.remoteLogLevel = it
        }
    }

    private suspend fun getLuckyLogLevel(luckyLogger: LuckyLogger?): LogLevel? {
        return luckyLogger?.let {
            sdkLogger.debug("applyLuckyLogger")
            val randomNumber = randomProvider.provide()
            if (it.threshold != 0.0 && randomNumber <= it.threshold) {
                it.logLevel
            } else {
                null
            }
        }
    }

    private suspend fun applyFeatures(features: RemoteConfigFeatures?, override: RemoteConfigFeatures? = null, sdkDisabled: Boolean? = false) {
        sdkLogger.debug("applyFeatures ${override?.let { "with override" } ?: ""}")
        if (sdkDisabled == true) {
            switch(MobileEngage, false)
            switch(EmbeddedMessaging, false)
            switch(JsBridgeSignatureCheck, false)
            throw SdkDisabledException("SDK is disabled!")
        } else {
            (override?.mobileEngage ?: features?.mobileEngage)?.let {
                switch(MobileEngage, it)
            }
            (override?.embeddedMessaging ?: features?.embeddedMessaging)?.let {
                switch(EmbeddedMessaging, it)
            }
            (override?.jsBridgeSignatureCheck ?: features?.jsBridgeSignatureCheck)?.let {
                switch(JsBridgeSignatureCheck, it)
            }
        }
    }

    private fun switch(feature: Features, to: Boolean) {
        if (to) {
            sdkContext.features.add(feature)
        } else {
            sdkContext.features.remove(feature)
        }
    }

    private suspend fun applyEmbeddedMessagingConfig(config: EmbeddedMessagingConfig?, override: EmbeddedMessagingConfig? = null) {
        sdkLogger.debug("applyEmbeddedMessagingConfig ${override?.let { "with override" } ?: ""}")
        (override?.tagUpdateBatchSize ?: config?.tagUpdateBatchSize)?.let {
            embeddedMessagingContext.tagUpdateBatchSize = it
        }
        (override?.tagUpdateFrequencyCapSeconds ?: config?.tagUpdateFrequencyCapSeconds)?.let {
            embeddedMessagingContext.tagUpdateFrequencyCapSeconds = it
        }
    }
}