package com.sap.ec.remoteConfig

import com.sap.ec.core.log.LogLevel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface RemoteConfig {
    @Serializable
    data class Default(
        val logLevel: LogLevel,
        val luckyLogger: LuckyLogger? = null,
        val features: RemoteConfigFeatures,
        val embeddedMessagingConfig: EmbeddedMessagingConfig
    ) : RemoteConfig {
        companion object {
            val SDK = Default(
                logLevel = LogLevel.Error,
                features = RemoteConfigFeatures(
                    mobileEngage = false,
                    embeddedMessaging = false,
                    jsBridgeSignatureCheck = true
                ),
                embeddedMessagingConfig = EmbeddedMessagingConfig(
                    tagUpdateBatchSize = 10,
                    tagUpdateFrequencyCapSeconds = 5
                )
            )
        }
    }

    @Serializable
    sealed interface Response : RemoteConfig {
        @Serializable
        data class Global(
            val globalRemoteConfigApplicationCodeValidationRegex: String? = null,
            val globalServiceUrls: RemoteConfigServiceUrls.Global,
            val logLevel: LogLevel? = null,
            val luckyLogger: LuckyLogger? = null,
            val features: RemoteConfigFeatures? = null,
            val embeddedMessagingConfig: EmbeddedMessagingConfig? = null
        ) : Response

        @Serializable
        data class AppCodeBased(
            val disabled: Boolean? = false,
            @SerialName("serviceUrls")
            val appCodeBasedServiceUrls: RemoteConfigServiceUrls.AppCodeBased,
            val logLevel: LogLevel? = null,
            val luckyLogger: LuckyLogger? = null,
            val features: RemoteConfigFeatures? = null,
            val embeddedMessagingConfig: EmbeddedMessagingConfig? = null,
            val overrides: Map<String, Override>? = null
        ) : Response

        @Serializable
        data class Override(
            val serviceUrls: RemoteConfigServiceUrls.Optional? = null,
            val logLevel: LogLevel? = null,
            val features: RemoteConfigFeatures? = null,
            val embeddedMessagingConfig: EmbeddedMessagingConfig? = null
        )
    }
}