package com.sap.ec.remoteConfig

import com.sap.ec.context.Features
import com.sap.ec.context.Features.EmbeddedMessaging
import com.sap.ec.context.Features.JsBridgeSignatureCheck
import com.sap.ec.context.Features.MobileEngage
import com.sap.ec.context.SdkContextApi
import com.sap.ec.context.copyWith
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.log.LogConfigHolderApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.DoubleProvider
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi

internal class RemoteConfigResponseHandler(
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val logConfigHolder: LogConfigHolderApi,
    private val sdkContext: SdkContextApi,
    private val randomProvider: DoubleProvider,
    private val embeddedMessagingContext: EmbeddedMessagingContextApi,
    private val sdkLogger: Logger
) : RemoteConfigResponseHandlerApi {
    override suspend fun handle(config: RemoteConfigResponse?) {
        if (config == null) {
            sdkLogger.error("config is null")
            return
        }

        val clientId = deviceInfoCollector.getClientId()

        sdkLogger.debug("applyServiceUrls")
        applyServiceUrls(config.serviceUrls)
        sdkLogger.debug("applyLogLevel")
        applyLogLevel(config.logLevel)
        sdkLogger.debug("applyFeatures")
        applyFeatures(config.features)
        sdkLogger.debug("applyLuckyLogger")
        applyLuckyLogger(config.luckyLogger)
        sdkLogger.debug("applyEmbeddedMessagingConfig")
        applyEmbeddedMessagingConfig(config.embeddedMessagingConfig)

        config.overrides?.let {
            it[clientId]?.let { override ->
                sdkLogger.debug("override applyServiceUrls")
                applyServiceUrls(override.serviceUrls)
                sdkLogger.debug("override applyLogLevel")
                applyLogLevel(override.logLevel)
                sdkLogger.debug("override applyFeatures")
                applyFeatures(override.features)
                sdkLogger.debug("override applyEmbeddedMessagingConfig")
                applyEmbeddedMessagingConfig(override.embeddedMessagingConfig)
            }
        }
    }

    private fun applyServiceUrls(serviceUrls: ServiceUrls?) {
        serviceUrls?.let {
            sdkContext.defaultUrls = sdkContext.defaultUrls.copyWith(
                clientServiceBaseUrl = it.clientService,
                eventServiceBaseUrl = it.eventService,
                deepLinkBaseUrl = it.deepLinkService,
                embeddedMessagingBaseUrl = it.embeddedMessagingService,
                jsBridgeUrl = it.jsBridgeUrl,
                jsBridgeSignatureUrl = it.jsBridgeSignatureUrl,
            )
        }
    }

    private fun applyLogLevel(logLevel: LogLevel?) {
        logLevel?.let {
            logConfigHolder.remoteLogLevel = it
        }
    }

    private fun applyFeatures(features: RemoteConfigFeatures?) {
        features?.mobileEngage?.let { switch(MobileEngage, it) }
        features?.embeddedMessaging?.let { switch(EmbeddedMessaging, it) }
        features?.jsBridgeSignatureCheck?.let { switch(JsBridgeSignatureCheck, it) }
    }

    private fun applyLuckyLogger(luckyLogger: LuckyLogger?) {
        luckyLogger?.let {
            val randomNumber = randomProvider.provide()
            if (it.threshold != 0.0 && randomNumber <= it.threshold) {
                logConfigHolder.remoteLogLevel = it.logLevel
            }
        }
    }

    private fun applyEmbeddedMessagingConfig(config: EmbeddedMessagingConfig?) {
        config?.tagUpdateBatchSize?.let {
            embeddedMessagingContext.tagUpdateBatchSize = it
        }
        config?.tagUpdateFrequencyCapSeconds?.let {
            embeddedMessagingContext.tagUpdateFrequencyCapSeconds = it
        }
    }

    private fun switch(feature: Features, to: Boolean) {
        if (to) {
            sdkContext.features.add(feature)
        } else {
            sdkContext.features.remove(feature)
        }
    }
}