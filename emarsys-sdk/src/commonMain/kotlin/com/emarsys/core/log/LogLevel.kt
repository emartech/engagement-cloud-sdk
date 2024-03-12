package com.emarsys.core.log

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LogLevel {
    @SerialName("DEBUG")
    Debug,

    @SerialName("ERROR")
    Error
}