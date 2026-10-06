package com.sap.ec.remoteConfig

import com.sap.ec.context.Features
import com.sap.ec.context.Features.EmbeddedMessaging
import com.sap.ec.context.Features.JsBridgeSignatureCheck
import com.sap.ec.context.Features.MobileEngage
import com.sap.ec.context.SdkContextApi
import com.sap.ec.context.ServiceUrls.FeatureUrls
import com.sap.ec.context.ServiceUrls.GlobalFeatureUrls
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.log.LogConfigHolderApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.DoubleProvider
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import com.sap.ec.core.exceptions.SdkException.SdkDisabledException

internal class RemoteConfigResponseHandler(
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val logConfigHolder: LogConfigHolderApi,
    private val sdkContext: SdkContextApi,
    private val randomProvider: DoubleProvider,
    private val embeddedMessagingContext: EmbeddedMessagingContextApi,
    private val sdkLogger: Logger
) : RemoteConfigResponseHandlerApi {
    override suspend fun handle(config: RemoteConfigResponse, isSdkDefault: Boolean) {
        val clientId = deviceInfoCollector.getClientId()
        val override = config.overrides?.get(clientId)

        applyGlobalRemoteConfigApplicationCodeValidationRegex(config.globalRemoteConfigApplicationCodeValidationRegex)
        applyGlobalServiceUrls(config.globalServiceUrls, isSdkDefault)
        applyServiceUrls(config.serviceUrls, override?.serviceUrls, isSdkDefault)
        applyLogLevel(config.logLevel, config.luckyLogger, override?.logLevel)
        applyFeatures(config.features, override?.features, config.disabled)
        applyEmbeddedMessagingConfig(config.embeddedMessagingConfig, override?.embeddedMessagingConfig)
    }

    private suspend fun applyGlobalRemoteConfigApplicationCodeValidationRegex(regex: String?) {
        regex?.let { regexString ->
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

    private suspend fun applyGlobalServiceUrls(globalServiceUrls: RemoteConfigGlobalServiceUrls?, isSdkDefault: Boolean) {
        sdkLogger.debug("applyGlobalServiceUrls")
        if (globalServiceUrls != null || isSdkDefault) {
            sdkContext.serviceUrls.globalFeatureUrls = globalServiceUrls?.let {
                GlobalFeatureUrls(
                    deepLinkBaseUrl = it.deepLinkService,
                    jsBridgeUrl = it.jsBridgeUrl,
                    jsBridgeSignatureUrl = it.jsBridgeSignatureUrl,
                )
            }
        }
    }

    private suspend fun applyServiceUrls(serviceUrls: RemoteConfigServiceUrls?, override: RemoteConfigOptionalServiceUrls?, isSdkDefault: Boolean) {
        sdkLogger.debug("applyServiceUrls ${override?.let { "with override" } ?: ""}")
        if (serviceUrls != null || isSdkDefault) {
            sdkContext.serviceUrls.featureUrls = serviceUrls?.let {
                FeatureUrls(
                    clientServiceBaseUrl = override?.clientService ?: it.clientService,
                    eventServiceBaseUrl = override?.eventService ?: it.eventService,
                    embeddedMessagingBaseUrl = override?.embeddedMessagingService ?: it.embeddedMessagingService,
                    loggingUrl = override?.loggingService ?: it.loggingService
                )
            }
        }
    }

    private suspend fun applyLogLevel(logLevel: LogLevel?, luckyLogger: LuckyLogger?, override: LogLevel?) {
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

    private suspend fun applyFeatures(features: RemoteConfigFeatures?, override: RemoteConfigFeatures?, sdkDisabled: Boolean? = false) {
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

    private suspend fun applyEmbeddedMessagingConfig(config: EmbeddedMessagingConfig?, override: EmbeddedMessagingConfig?) {
        sdkLogger.debug("applyEmbeddedMessagingConfig ${override?.let { "with override" } ?: ""}")
        (override?.tagUpdateBatchSize ?: config?.tagUpdateBatchSize)?.let {
            embeddedMessagingContext.tagUpdateBatchSize = it
        }
        (override?.tagUpdateFrequencyCapSeconds ?: config?.tagUpdateFrequencyCapSeconds)?.let {
            embeddedMessagingContext.tagUpdateFrequencyCapSeconds = it
        }
    }
}