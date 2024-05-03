package com.emarsys.core.log

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LogLevel {
    @SerialName("INFO")
    Info,

    @SerialName("DEBUG")
    Debug,

    @SerialName("ERROR")
    Error
}