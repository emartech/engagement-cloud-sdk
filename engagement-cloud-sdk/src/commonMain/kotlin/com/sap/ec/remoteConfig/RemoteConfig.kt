package com.sap.ec.remoteConfig

import com.sap.ec.core.log.LogLevel
import kotlinx.serialization.Serializable

@Serializable
data class RemoteConfig(
    val serviceUrls: ServiceUrls? = null,
    val logLevel: LogLevel? = null,
    val features: RemoteConfigFeatures? = null,
)
