package com.emarsys.remoteConfig

import com.emarsys.core.log.LogLevel
import kotlinx.serialization.Serializable

@Serializable
data class LuckyLogger(
    val logLevel: LogLevel,
    val threshold: Double
)
