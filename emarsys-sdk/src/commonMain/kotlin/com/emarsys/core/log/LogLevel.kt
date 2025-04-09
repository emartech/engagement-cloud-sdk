package com.emarsys.core.log

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LogLevel(val priority:Int) {
    @SerialName("TRACE")
    Trace(0),

    @SerialName("DEBUG")
    Debug(1),

    @SerialName("INFO")
    Info(2),

    @SerialName("ERROR")
    Error(3),

    @SerialName("METRIC")
    Metric(4)
}