package com.emarsys.remoteConfig

import com.emarsys.context.Features
import com.emarsys.context.SdkContextApi
import com.emarsys.context.copyWith
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.LogLevel
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClientApi
import com.emarsys.providers.Provider

class RemoteConfigHandler(
    private val remoteConfigClient: RemoteConfigClientApi,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val sdkContext: SdkContextApi,
    private val randomProvider: Provider<Double>
) : RemoteConfigHandlerApi {
    override suspend fun handle() {
        val config = remoteConfigClient.fetchRemoteConfig() ?: return
        val hardwareId = deviceInfoCollector.getHardwareId()

        applyServiceUrls(config.serviceUrls)
        applyLogLevel(config.logLevel)
        applyFeatures(config.features)
        applyLuckyLogger(config.luckyLogger)

        config.overrides?.let {
            it[hardwareId]?.let { override ->
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
        features?.let {
            it.mobileEngage?.let { hasMobileEngage ->
                switch(Features.MOBILE_ENGAGE, hasMobileEngage)
            }
            it.predict?.let { hasPredict ->
                switch(Features.PREDICT, hasPredict)
            }
        }
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