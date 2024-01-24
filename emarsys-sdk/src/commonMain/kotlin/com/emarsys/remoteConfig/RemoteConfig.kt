package com.emarsys.remoteConfig

import com.emarsys.core.log.LogLevel
import kotlinx.serialization.Serializable

@Serializable
data class RemoteConfig(
    val serviceUrls: ServiceUrls? = null,
    val logLevel: LogLevel? = null,
    val features: RemoteConfigFeatures? = null,
)
