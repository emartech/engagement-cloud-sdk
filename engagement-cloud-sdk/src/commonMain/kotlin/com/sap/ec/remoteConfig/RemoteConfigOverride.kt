package com.sap.ec.remoteConfig

import com.sap.ec.core.log.LogLevel
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteConfigOverride(
    val serviceUrls: RemoteConfigOptionalServiceUrls? = null,
    val logLevel: LogLevel? = null,
    val features: RemoteConfigFeatures? = null,
    val embeddedMessagingConfig: EmbeddedMessagingConfig? = null
)