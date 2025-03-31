package com.emarsys.core.log

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LogLevel {
    @SerialName("TRACE")
    Trace,

    @SerialName("DEBUG")
    Debug,

    @SerialName("INFO")
    Info,

    @SerialName("ERROR")
    Error,

    @SerialName("METRIC")
    Metric
}