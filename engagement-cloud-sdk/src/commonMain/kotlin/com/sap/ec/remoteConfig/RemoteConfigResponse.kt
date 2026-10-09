package com.sap.ec.remoteConfig

import com.sap.ec.core.log.LogLevel
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteConfigResponse(
    val globalRemoteConfigApplicationCodeValidationRegex: String? = null,
    val globalServiceUrls: RemoteConfigGlobalServiceUrls? = null,
    val serviceUrls: RemoteConfigServiceUrls? = null,
    val logLevel: LogLevel? = null,
    val luckyLogger: LuckyLogger? = null,
    val features: RemoteConfigFeatures? = null,
    val embeddedMessagingConfig: EmbeddedMessagingConfig? = null,
    val overrides: Map<String, RemoteConfigOverride>? = null,
    val disabled: Boolean? = false
) {
    companion object {
        val SDK_DEFAULT = RemoteConfigResponse(
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