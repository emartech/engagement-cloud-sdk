package com.emarsys.remoteConfig

import com.emarsys.context.Features
import com.emarsys.context.SdkContextApi
import com.emarsys.context.copyWith
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.DoubleProvider
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClientApi

internal class RemoteConfigHandler(
    private val remoteConfigClient: RemoteConfigClientApi,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val sdkContext: SdkContextApi,
    private val randomProvider: DoubleProvider,
    private val sdkLogger: Logger
) : RemoteConfigHandlerApi {
    private suspend fun handle(config: RemoteConfigResponse?, clientId: String?) {
        if (config == null) {
            sdkLogger.error("RemoteConfigHandler - handle", "config is null")
            return
        }

        sdkLogger.debug("RemoteConfigHandler - handle", "applyServiceUrls")
        applyServiceUrls(config.serviceUrls)
        sdkLogger.debug("RemoteConfigHandler - handle", "applyLogLevel")
        applyLogLevel(config.logLevel)
        sdkLogger.debug("RemoteConfigHandler - handle", "applyFeatures")
        applyFeatures(config.features)
        sdkLogger.debug("RemoteConfigHandler - handle", "applyLuckyLogger")
        applyLuckyLogger(config.luckyLogger)

        config.overrides?.let {
            it[clientId]?.let { override ->
                sdkLogger.debug("RemoteConfigHandler - handle", "override applyServiceUrls")
                applyServiceUrls(override.serviceUrls)
                sdkLogger.debug("RemoteConfigHandler - handle", "override applyLogLevel")
                applyLogLevel(override.logLevel)
                sdkLogger.debug("RemoteConfigHandler - handle", "override applyFeatures")
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