package com.sap.ec.core.device

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


interface IosNotificationConstant {
    val value: Long

    companion object {
        inline fun <reified T> fromLong(value: Long?): T where T : Enum<T>, T : IosNotificationConstant {
            return enumValues<T>().find { it.value == (value ?: 0L) }!!
        }
    }
}

@Serializable
enum class IosAuthorizationStatus(override val value: Long) : IosNotificationConstant {
    @SerialName("notDetermined")
    NotDetermined(0),

    @SerialName("denied")
    Denied(1),

    @SerialName("authorized")
    Authorized(2),

    @SerialName("provisional")
    Provisional(3),

    @SerialName("ephemeral")
    Ephemeral(4)
}

@Serializable
enum class IosNotificationSetting(override val value: Long) : IosNotificationConstant {
    @SerialName("notSupported")
    NotSupported(0),

    @SerialName("disabled")
    Disabled(1),

    @SerialName("enabled")
    Enabled(2)
}

@Serializable
enum class IosAlertStyle(override val value: Long) : IosNotificationConstant {
    @SerialName("none")
    None(0),

    @SerialName("banner")
    Banner(1),

    @SerialName("alert")
    Alert(2)
}

@Serializable
enum class IosShowPreviewSetting(override val value: Long) : IosNotificationConstant {
    @SerialName("always")
    Always(0),

    @SerialName("whenAuthenticated")
    WhenAuthenticated(1),

    @SerialName("never")
    Never(2)
}

fun String.toShowPreviewSetting() : IosShowPreviewSetting {
    return when (this) {
        "UNShowPreviewsSettingAlways" -> IosShowPreviewSetting.Always
        "UNShowPreviewsSettingWhenAuthenticated" -> IosShowPreviewSetting.WhenAuthenticated
        "UNShowPreviewsSettingNever" -> IosShowPreviewSetting.Never
        else -> IosShowPreviewSetting.Always
    }
}