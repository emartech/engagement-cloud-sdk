package com.emarsys.remoteConfig

import com.emarsys.context.Features
import com.emarsys.context.SdkContextApi
import com.emarsys.context.copyWith
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.DoubleProvider

internal class RemoteConfigResponseHandler(
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val sdkContext: SdkContextApi,
    private val randomProvider: DoubleProvider,
    private val sdkLogger: Logger
) : RemoteConfigResponseHandlerApi {
    override suspend fun handle(response: RemoteConfigResponse?) {
        if (response == null) {
            sdkLogger.error("config is null")
            return
        }

        val clientId = deviceInfoCollector.getClientId()

        sdkLogger.debug("applyServiceUrls")
        applyServiceUrls(response.serviceUrls)
        sdkLogger.debug("applyLogLevel")
        applyLogLevel(response.logLevel)
        sdkLogger.debug("applyFeatures")
        applyFeatures(response.features)
        sdkLogger.debug("applyLuckyLogger")
        applyLuckyLogger(response.luckyLogger)

        response.overrides?.let {
            it[clientId]?.let { override ->
                sdkLogger.debug("override applyServiceUrls")
                applyServiceUrls(override.serviceUrls)
                sdkLogger.debug("override applyLogLevel")
                applyLogLevel(override.logLevel)
                sdkLogger.debug("override applyFeatures")
                applyFeatures(override.features)
            }
        }
    }

    private fun applyServiceUrls(serviceUrls: ServiceUrls?) {
        serviceUrls?.let {
            sdkContext.defaultUrls = sdkContext.defaultUrls.copyWith(
                clientServiceBaseUrl = it.clientService,
                eventServiceBaseUrl = it.eventService,
                deepLinkBaseUrl = it.deepLinkService,
                inboxBaseUrl = it.inboxService
            )
        }
    }

    private fun applyLogLevel(logLevel: LogLevel?) {
        logLevel?.let {
            sdkContext.remoteLogLevel = it
        }
    }

    private fun applyFeatures(features: RemoteConfigFeatures?) {
        features?.mobileEngage?.let { switch(Features.MOBILE_ENGAGE, it) }
    }

    private fun applyLuckyLogger(luckyLogger: LuckyLogger?) {
        luckyLogger?.let {
            val randomNumber = randomProvider.provide()
            if (it.threshold != 0.0 && randomNumber <= it.threshold) {
                sdkContext.remoteLogLevel = it.logLevel
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
}