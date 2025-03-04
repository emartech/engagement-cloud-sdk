package com.emarsys.remoteConfig

import com.emarsys.context.Features
import com.emarsys.context.SdkContextApi
import com.emarsys.context.copyWith
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClientApi

class RemoteConfigHandler(
    private val remoteConfigClient: RemoteConfigClientApi,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val sdkContext: SdkContextApi,
    private val randomProvider: Provider<Double>,
    private val logger: Logger
) : RemoteConfigHandlerApi {
    private suspend fun handle(config: RemoteConfigResponse?, clientId: String?) {
        if (config == null) {
            logger.error("RemoteConfigHandler - handle", "config is null")
            return
        }

        applyServiceUrls(config.serviceUrls)
        applyLogLevel(config.logLevel)
        applyFeatures(config.features)
        applyLuckyLogger(config.luckyLogger)

        config.overrides?.let {
            it[clientId]?.let { override ->
                applyServiceUrls(override.serviceUrls)
                applyLogLevel(override.logLevel)
                applyFeatures(override.features)
            }
        }
    }

    private fun applyServiceUrls(serviceUrls: ServiceUrls?) {
        serviceUrls?.let {
            sdkContext.defaultUrls = sdkContext.defaultUrls.copyWith(
                clientServiceBaseUrl = it.clientService,
                eventServiceBaseUrl = it.eventService,
                predictBaseUrl = it.predictService,
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
        features?.predict?.let { switch(Features.PREDICT, it) }
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

    override suspend fun handleAppCodeBased() {
        val config = remoteConfigClient.fetchRemoteConfig() ?: return
        val clientId = deviceInfoCollector.getClientId()
        handle(config, clientId)
    }

    override suspend fun handleGlobal() {
        val config = remoteConfigClient.fetchRemoteConfig(true) ?: return
        val clientId = deviceInfoCollector.getClientId()

        handle(config, clientId)
    }

}