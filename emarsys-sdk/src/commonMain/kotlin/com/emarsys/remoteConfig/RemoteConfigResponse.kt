package com.emarsys.remoteConfig

import com.emarsys.core.log.LogLevel
import kotlinx.serialization.Serializable

@Serializable
data class RemoteConfigResponse(
    val serviceUrls: ServiceUrls? = null,
    val logLevel: LogLevel? = null,
    val luckyLogger: LuckyLogger? = null,
    val features: RemoteConfigFeatures? = null,
    val overrides: Map<String, RemoteConfig>? = null
)