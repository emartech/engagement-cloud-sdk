package com.sap.ec.core.device

import kotlinx.serialization.SerialName

enum class DeviceCategory {
    @SerialName("bot")
    BOT,

    @SerialName("desktop")
    DESKTOP,

    @SerialName("mobile")
    MOBILE,

    @SerialName("tablet")
    TABLET,

    @SerialName("tv")
    TV,

    @SerialName("unknown")
    UNKNOWN
}
