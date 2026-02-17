package com.sap.ec.remoteConfig

import com.sap.ec.core.log.LogLevel
import kotlinx.serialization.Serializable

@Serializable
data class LuckyLogger(
    val logLevel: LogLevel,
    val threshold: Double
)
